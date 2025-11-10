package com.napier.sem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Application demonstrating JDBC queries and formatted output.
 */
public final class App {

    /** Maximum rows to print in tables. */
    private static final int MAX_ROWS = 10;

    /** Total connect timeout in ms. */
    private static final int CONNECT_TIMEOUT_MS = 100_000;

    /** Max retry attempts (safety cap). */
    private static final int MAX_RETRIES = 1000;

    /** Delay between retries in ms (used by main/demo). */
    private static final int RETRY_DELAY_MS = 10000;

    /** Active JDBC connection (opened in connect). */
    private Connection con;

    /** Default constructor. */
    public App() { }

    /**
     * Connect to the database with retries.
     *
     * @param location JDBC URL (must include host:port/dbname)
     * @param delay    milliseconds between retries
     */
    public void connect(final String location, final int delay) {
        final long deadline = System.currentTimeMillis() + CONNECT_TIMEOUT_MS;

        // Read credentials from env (overrideable in CI/IDE)
        final String user = System.getenv().getOrDefault("DB_USER", "app");
        final String pass = System.getenv().getOrDefault("DB_PASS", "example");

        int attempt = 0;
        while (System.currentTimeMillis() < deadline && attempt < MAX_RETRIES) {
            attempt++;
            System.out.println("Connecting time" + attempt);
            try {
                con = DriverManager.getConnection(location, user, pass);
                System.out.println("Connected as " + user);
                return;
            } catch (SQLException e) {
                System.out.println("Connect failed: " + e.getMessage());
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
        System.out.println("Could not connect to database.");
    }

    /** Disconnects from the database safely. */
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("Close failed: " + e.getMessage());
            }
        }
    }

    /**
     * Display employees result rows in a fixed format.
     *
     * @param employees result set to iterate
     * @throws SQLException when reading columns fails
     */
    public void displayEmployees(final ResultSet employees)
            throws SQLException {
        System.out.printf("%-10s %-15s %-15s %-10s%n",
                "Emp No", "First Name", "Last Name", "Salary");

        int printed = 0;
        while (employees.next() && printed < MAX_ROWS) {
            final String row = String.format(
                    "%-10d %-15s %-15s %-10d",
                    employees.getInt("emp_no"),
                    employees.getString("first_name"),
                    employees.getString("last_name"),
                    employees.getInt("salary")
            );
            System.out.println(row);
            printed++;
        }
    }

    /**
     * Print a list of Employee DTOs (used by unit tests).
     *
     * @param employees list of employees; may be {@code null} or contain nulls
     */
    public void printSalaries(final List<Employee> employees) {
        System.out.printf("%-10s %-15s %-15s %-10s%n",
                "Emp No", "First Name", "Last Name", "Salary");

        if (employees == null || employees.isEmpty()) {
            return;
        }

        int printed = 0;
        for (Employee e : employees) {
            if (e == null) {
                continue;
            }
            final int no = e.getEmpNo();
            final String fn = e.getFirstName() == null ? "" : e.getFirstName();
            final String ln = e.getLastName() == null ? "" : e.getLastName();
            final int sal = e.getSalary();

            System.out.printf("%-10d %-15s %-15s %-10d%n", no, fn, ln, sal);

            printed++;
            if (printed >= MAX_ROWS) {
                break;
            }
        }
    }

    /**
     * Fetch a single employee by id.
     *
     * @param id employee number
     * @return a ResultSet for the requested employee (latest salary)
     * @throws SQLException if a JDBC error occurs
     */
    public ResultSet getEmployee(final int id) throws SQLException {
        if (con == null) {
            throw new SQLException("Connection is null");
        }
        final Statement stmt = con.createStatement();
        final String sql =
                "SELECT e.emp_no, e.first_name, e.last_name, s.salary "
                        + "FROM employees.employees e "
                        + "JOIN employees.salaries s ON e.emp_no = s.emp_no "
                        + "WHERE e.emp_no = " + id + " "
                        + "ORDER BY s.to_date DESC LIMIT 1";
        return stmt.executeQuery(sql);
    }

    /**
     * Salaries by role (title).
     *
     * @param role title name
     * @return ResultSet with rows ordered by salary DESC
     * @throws SQLException database error
     */
    public ResultSet getSalariesByRole(final String role)
            throws SQLException {
        if (con == null) {
            throw new SQLException("Connection is null");
        }
        final Statement stmt = con.createStatement();
        final String sql =
                "SELECT e.emp_no, e.first_name, e.last_name, s.salary "
                        + "FROM employees.employees e "
                        + "JOIN employees.titles t ON e.emp_no = t.emp_no "
                        + "JOIN employees.salaries s ON e.emp_no = s.emp_no "
                        + "WHERE t.title = '" + role + "' "
                        + "AND s.to_date = '9999-01-01' "
                        + "ORDER BY s.salary DESC";
        return stmt.executeQuery(sql);
    }

    /**
     * Look up a department from a string id (convenience overload).
     *
     * @param deptNoStr department id as string
     * @return Department instance or {@code null} if not found/invalid
     */
    public Department getDepartment(final String deptNoStr) {
        if (deptNoStr == null || deptNoStr.isBlank()) {
            return null;
        }
        try {
            final int deptNo = Integer.parseInt(deptNoStr.trim());
            return getDepartment(deptNo);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * Fetch a Department (with current manager if available).
     *
     * @param deptNo numeric department id
     * @return Department or {@code null} if not found
     */
    public Department getDepartment(final int deptNo) {
        if (con == null) {
            throw new IllegalStateException(
                    "Database connection is not established.");
        }

        final String sql =
                "SELECT d.dept_no, d.dept_name, "
                        + "       e.first_name AS manager_first, "
                        + "       e.last_name  AS manager_last "
                        + "FROM departments d "
                        + "LEFT JOIN dept_manager dm "
                        + "  ON d.dept_no = dm.dept_no "
                        + " AND dm.to_date = '9999-01-01' "
                        + "LEFT JOIN employees e "
                        + "  ON dm.emp_no = e.emp_no "
                        + "WHERE d.dept_no = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, deptNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                final Department d = new Department();
                d.setDeptNo(rs.getInt("dept_no"));
                d.setDeptName(rs.getString("dept_name"));

                final String mf = rs.getString("manager_first");
                final String ml = rs.getString("manager_last");

                if (mf != null && ml != null) {
                    d.setManager(mf + " " + ml);
                } else {
                    d.setManager("N/A");
                }
                return d;
            }
        } catch (SQLException ex) {
            System.err.println("getDepartment failed: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Salaries aggregated by department.
     *
     * @param dept department object (not null)
     * @return ResultSet of employees in the department ordered by salary
     * @throws SQLException on error
     */
    public ResultSet getSalariesByDepartment(final Department dept)
            throws SQLException {
        if (con == null) {
            throw new SQLException("Connection is null");
        }
        final Statement stmt = con.createStatement();
        final String sql =
                "SELECT e.emp_no, e.first_name, e.last_name, s.salary "
                        + "FROM employees.employees e "
                        + "JOIN employees.dept_emp de ON e.emp_no = de.emp_no "
                        + "JOIN employees.salaries s ON e.emp_no = s.emp_no "
                        + "WHERE de.dept_no = '" + dept.getDeptNo() + "' "
                        + "AND s.to_date = '9999-01-01' "
                        + "ORDER BY s.salary DESC";
        return stmt.executeQuery(sql);
    }

    /**
     * Pretty-print a salaries result set.
     *
     * @param rows result set
     * @throws SQLException on read error
     */
    public void displaySalaries(final ResultSet rows) throws SQLException {
        System.out.printf("%-10s %-15s %-15s %-10s%n",
                "Emp No", "First Name", "Last Name", "Salary");
        int printed = 0;
        while (rows.next() && printed < MAX_ROWS) {
            System.out.printf("%-10d %-15s %-15s %-10d%n",
                    rows.getInt("emp_no"),
                    rows.getString("first_name"),
                    rows.getString("last_name"),
                    rows.getInt("salary"));
            printed++;
        }
        if (printed == 0) {
            System.out.println("No employees");
        }
    }

    /**
     * Entry point used by Docker image (demo).
     */
    public static void main(final String[] args) {
        final App a = new App();
        // Host-mapped port; adjust if your compose uses another one.
        final String url =
                "jdbc:mysql://127.0.0.1:35432/employees"
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        a.connect(url, RETRY_DELAY_MS);
        a.disconnect();
    }
}
