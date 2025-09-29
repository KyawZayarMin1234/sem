package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    /** Connection to MySQL database. */
    private Connection con = null;

    // Read env var with default
    private static String env(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    /** Connect to the MySQL database. */
    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        // NOTE:
        // - If running Java on your Windows host: set DB_HOST=127.0.0.1 and DB_PORT=33070
        // - If running Java in Docker on the same network as MySQL: set DB_HOST to the service/container name (e.g., setmethods-db-1) and DB_PORT=3306
        String host = env("DB_HOST", "127.0.0.1");
        String port = env("DB_PORT", "33070");
        String db   = env("DB_NAME", "employees");
        String user = env("DB_USER", "app");
        String pass = env("DB_PASSWORD", "example");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        int retries = 10;
        for (int i = 1; i <= retries; i++) {
            System.out.printf("Connecting to database (attempt %d/%d)...%n", i, retries);
            try {
                con = DriverManager.getConnection(url, user, pass);
                System.out.println("Successfully connected");
                return;
            } catch (SQLException e) {
                System.out.println("Failed: " + e.getMessage());
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
        System.out.println("Could not connect to database. Exiting.");
        System.exit(1);
    }

    /** Disconnect from the MySQL database. */
    public void disconnect() {
        if (con != null) {
            try { con.close(); } catch (Exception ignored) {}
        }
    }

    /** Tutorial method (already in your file): fetch one employee with extra info. */
    public Employee getEmployee(int id) {
        if (con == null) return null;

        String sql = """
            SELECT e.emp_no, e.first_name, e.last_name,
                   t.title,
                   s.salary,
                   d.dept_name,
                   CONCAT(m.first_name, ' ', m.last_name) AS manager
            FROM employees.employees e
            LEFT JOIN employees.titles t
                   ON t.emp_no = e.emp_no AND t.to_date = '9999-01-01'
            LEFT JOIN employees.salaries s
                   ON s.emp_no = e.emp_no AND s.to_date = '9999-01-01'
            LEFT JOIN employees.dept_emp de
                   ON de.emp_no = e.emp_no AND de.to_date = '9999-01-01'
            LEFT JOIN employees.departments d
                   ON d.dept_no = de.dept_no
            LEFT JOIN employees.dept_manager dm
                   ON dm.dept_no = de.dept_no AND dm.to_date = '9999-01-01'
            LEFT JOIN employees.employees m
                   ON m.emp_no = dm.emp_no
            WHERE e.emp_no = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Employee emp = new Employee();
                emp.emp_no = rs.getInt("emp_no");
                emp.first_name = rs.getString("first_name");
                emp.last_name  = rs.getString("last_name");
                emp.title      = rs.getString("title");
                emp.salary     = rs.getInt("salary");
                emp.dept_name  = rs.getString("dept_name");
                emp.manager    = rs.getString("manager");
                return emp;
            }
        } catch (SQLException e) {
            System.out.println("Failed to get employee details: " + e.getMessage());
            return null;
        }
    }

    public void displayEmployee(Employee emp) {
        if (emp == null) {
            System.out.println("No employee found.");
            return;
        }
        System.out.println(
                emp.emp_no + " " + emp.first_name + " " + emp.last_name + "\n" +
                        (emp.title == null ? "N/A" : emp.title) + "\n" +
                        "Salary: " + emp.salary + "\n" +
                        (emp.dept_name == null ? "N/A" : emp.dept_name) + "\n" +
                        "Manager: " + (emp.manager == null ? "N/A" : emp.manager) + "\n"
        );
    }

    /* ---------------------------------------------------------------------- */
    /* UC4: Salaries by Role                                                  */
    /* ---------------------------------------------------------------------- */

    /** Returns current salaries of all employees who have the given title. */
    public List<Employee> getSalariesByRole(String role) {
        List<Employee> list = new ArrayList<>();
        if (con == null) return list;

        String sql = """
            SELECT e.emp_no, e.first_name, e.last_name, s.salary
            FROM employees.employees e
            JOIN employees.salaries s ON e.emp_no = s.emp_no
            JOIN employees.titles   t ON e.emp_no = t.emp_no
            WHERE s.to_date = '9999-01-01'
              AND t.to_date = '9999-01-01'
              AND t.title = ?
            ORDER BY e.emp_no ASC
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee();
                    emp.emp_no     = rs.getInt("emp_no");
                    emp.first_name = rs.getString("first_name");
                    emp.last_name  = rs.getString("last_name");
                    emp.salary     = rs.getInt("salary");
                    emp.title      = role;
                    list.add(emp);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get salaries by role: " + e.getMessage());
        }
        return list;
    }

    public void displaySalaries(List<Employee> rows) {
        if (rows == null || rows.isEmpty()) {
            System.out.println("No results.");
            return;
        }
        System.out.printf("%-8s %-15s %-15s %10s%n",
                "emp_no", "first_name", "last_name", "salary");
        for (Employee r : rows) {
            System.out.printf("%-8d %-15s %-15s %10d%n",
                    r.emp_no, r.first_name, r.last_name, r.salary);
        }
    }

    /* ---------------------------------------------------------------------- */

    public static void main(String[] args) {
        App a = new App();
        a.connect();

        // --- UC4 demo: pass a title via args or type it interactively ---
        String role = (args.length > 0) ? args[0] : null;
        if (role == null) {
            System.out.print("Enter title (e.g., Engineer): ");
            role = new Scanner(System.in).nextLine();
        }
        System.out.println("Salaries for role: " + role);
        a.displaySalaries(a.getSalariesByRole(role));

        // --- existing single-employee demo (optional) ---
        // Employee emp = a.getEmployee(10001);
        // a.displayEmployee(emp);

        a.disconnect();
    }
}
