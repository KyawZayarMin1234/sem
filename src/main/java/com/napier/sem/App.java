package com.napier.sem;

import java.sql.*;

public class App {
    /** Connection to MySQL database. */
    private Connection con = null;

    private static String env(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    /** Connect to the MySQL database. */
    public void connect() {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver"); System.exit(-1);
        }

        String host = env("DB_HOST", "db");
        String port = env("DB_PORT", "3306");
        String db   = env("DB_NAME", "employees");
        String user = env("DB_USER", "app");      // use the non-root user you created
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

    // We'll add getEmployee(...) and displayEmployee(...) below.
    /** Basic version: just emp_no, first_name, last_name */
    public Employee getEmployeeBasic(int id) {
        if (con == null) return null;
        String sql = """
            SELECT emp_no, first_name, last_name
            FROM employees.employees
            WHERE emp_no = ?
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Employee e = new Employee();
                e.emp_no = rs.getInt("emp_no");
                e.first_name = rs.getString("first_name");
                e.last_name  = rs.getString("last_name");
                return e;
            }
        } catch (SQLException e) {
            System.out.println("Failed to get employee details: " + e.getMessage());
            return null;
        }
    }


    /** Full version: returns current title/salary/department/manager too */
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
                   ON t.emp_no = e.emp_no
                  AND t.to_date = '9999-01-01'
            LEFT JOIN employees.salaries s
                   ON s.emp_no = e.emp_no
                  AND s.to_date = '9999-01-01'
            LEFT JOIN employees.dept_emp de
                   ON de.emp_no = e.emp_no
                  AND de.to_date = '9999-01-01'
            LEFT JOIN employees.departments d
                   ON d.dept_no = de.dept_no
            LEFT JOIN employees.dept_manager dm
                   ON dm.dept_no = de.dept_no
                  AND dm.to_date = '9999-01-01'
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
        // using the fields explicitly (tutorial style):
        System.out.println(
                emp.emp_no + " " + emp.first_name + " " + emp.last_name + "\n" +
                        (emp.title == null ? "N/A" : emp.title) + "\n" +
                        "Salary: " + emp.salary + "\n" +
                        (emp.dept_name == null ? "N/A" : emp.dept_name) + "\n" +
                        "Manager: " + (emp.manager == null ? "N/A" : emp.manager) + "\n"
        );

        // or simply:
        // System.out.println(emp);
    }





    public static void main(String[] args) {
        App a = new App();
        a.connect();

        // demo: try a known ID from the dataset (10001 exists)
        Employee emp = a.getEmployee(10001);
        a.displayEmployee(emp);

        a.disconnect();
    }
}
