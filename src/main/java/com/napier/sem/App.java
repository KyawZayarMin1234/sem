package com.napier.sem;

import java.sql.*;

public class App {
    public static void main(String[] args) {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver"); System.exit(-1);
        }

        String host = getenvOr("DB_HOST", "db");
        String port = getenvOr("DB_PORT", "3306");
        String db   = getenvOr("DB_NAME", "employees");
        String user = getenvOr("DB_USER", "root");
        String pass = getenvOr("DB_PASSWORD", "example");

        String url = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port, db
        );

        Connection con = null;
        int retries = 10; // no need for 100 and 30s waits
        for (int i = 1; i <= retries; i++) {
            System.out.printf("Connecting to database (attempt %d/%d)...%n", i, retries);
            try {
                con = DriverManager.getConnection(url, user, pass);
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed: " + sqle.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }

        if (con == null) {
            System.out.println("Could not connect to database. Exiting.");
            System.exit(1);
        }

        // quick smoke test: count employees
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS cnt FROM employees.employees")) {
            if (rs.next()) System.out.println("employees count = " + rs.getLong("cnt"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try { con.close(); } catch (Exception ignored) {}
    }

    private static String getenvOr(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
