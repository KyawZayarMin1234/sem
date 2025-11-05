package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main application and simple reporting methods used by unit tests.
 */
public class App {
    /** Connection to MySQL database. */
    private Connection con = null;

    // ---------- env helper ----------
    private static String env(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    // ---------- DB connect/disconnect ----------
    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        // Default for "Java running on Windows host against Docker MySQL"
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

    public void disconnect() {
        if (con != null) {
            try { con.close(); } catch (Exception ignored) {}
        }
    }

    // ---------- LAB: printSalaries (the method your test calls) ----------
    /**
     * Prints a salary report for a list of employees.
     * Lab requirements:
     *  - If employees == null → print "No employees" and return (no exception).
     *  - If list is empty → print header only.
     *  - Skip any null entries.
     */
    public void printSalaries(ArrayList<Employee> employees) {
        // Check employees is not null
        if (employees == null) {
            System.out.println("No employees");
            return;
        }
        // Print header
        System.out.println(String.format("%-10s %-15s %-20s %-8s",
                "Emp No", "First Name", "Last Name", "Salary"));

        // Loop over all employees in the list
        for (Employee emp : employees) {
            if (emp == null) continue; // be safe for later tests
            String emp_string = String.format("%-10s %-15s %-20s %-8s",
                    emp.emp_no, emp.first_name, emp.last_name, emp.salary);
            System.out.println(emp_string);
        }
    }

    // ---------- Optional helpers from your app (kept minimal and compile-safe) ----------
    public Employee getEmployee(int id) {
        if (con == null) return null;

        // Keep this simple and compile-safe; adjust SELECT columns to match reads.
        String sql = """
            SELECT e.emp_no, e.first_name, e.last_name
            FROM employees.employees e
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
                return emp;
            }
        } catch (SQLException e) {
            System.out.println("Failed to get employee details: " + e.getMessage());
            return null;
        }
    }

    public void displayEmployee(Employee emp) {
        if (emp == null) {
            System.out.println("Employee is null");
            return;
        }
        System.out.println("Employee Details");
        System.out.println("----------------");
        System.out.printf("Emp No   : %s%n", String.valueOf(emp.emp_no));
        System.out.printf("Name     : %s %s%n", safe(emp.first_name), safe(emp.last_name));
        System.out.printf("Title    : %s%n", safe(emp.title));
        System.out.printf("Salary   : %s%n", String.valueOf(emp.salary));
        System.out.printf("Dept     : %s%n",
                (emp.dept == null ? "N/A" : safe(emp.dept.dept_name)));
        System.out.printf("Manager  : %s%n",
                (emp.manager == null ? "N/A" : (safe(emp.manager.first_name) + " " + safe(emp.manager.last_name))));
    }

    // Example query group you had (left here to keep your app structure)
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

    public Department getDepartment(int dept_no) {
        String q = "SELECT dept_no, dept_name FROM employees.departments WHERE dept_no = ?";
        try (PreparedStatement ps = con.prepareStatement(q)) {
            ps.setInt(1, dept_no);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Department dept = new Department();
                dept.dept_no = rs.getInt("dept_no");
                dept.dept_name = rs.getString("dept_name");
                return dept;
            }
        } catch (SQLException e) {
            System.out.println("Failed to get department: " + e.getMessage());
            return null;
        }
    }

    public ArrayList<Employee> getSalariesByDepartment(Department dept) {
        ArrayList<Employee> list = new ArrayList<>();
        if (con == null || dept == null) return list;

        String sql = """
            SELECT e.emp_no, e.first_name, e.last_name,
                   s.salary, t.title
            FROM employees.employees e
            JOIN employees.salaries s
              ON e.emp_no = s.emp_no AND s.to_date = '9999-01-01'
            JOIN employees.titles t
              ON e.emp_no = t.emp_no AND t.to_date = '9999-01-01'
            JOIN employees.dept_emp de
              ON e.emp_no = de.emp_no AND de.to_date = '9999-01-01'
            WHERE de.dept_no = ?
            ORDER BY e.emp_no ASC
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dept.dept_no);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee();
                    emp.emp_no     = rs.getInt("emp_no");
                    emp.first_name = rs.getString("first_name");
                    emp.last_name  = rs.getString("last_name");
                    emp.salary     = rs.getInt("salary");
                    emp.title      = rs.getString("title");
                    emp.dept       = dept;
                    list.add(emp);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get salaries by department: " + e.getMessage());
        }
        return list;
    }

    // ---------- helpers ----------
    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    // ---------- single main (only one) ----------
    public static void main(String[] args) {
        App a = new App();
        a.connect();

        String role = (args.length > 0) ? args[0] : null;
        if (role == null) {
            System.out.print("Enter title (e.g., Engineer): ");
            role = new Scanner(System.in).nextLine();
        }
        System.out.println("Salaries for role: " + role);
        a.displaySalaries(a.getSalariesByRole(role));

        a.disconnect();
    }
}
