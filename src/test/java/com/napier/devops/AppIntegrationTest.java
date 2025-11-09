package com.napier.devops;

import com.napier.sem.App;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppIntegrationTest {
    static App app;

    @BeforeAll
    void init() throws Exception {
        app = new App();

        // Local default (published port 33070). In CI, set INT_DB_URL to "jdbc:mysql://db:3306/…"
        String url = System.getenv().getOrDefault(
                "INT_DB_URL",
                "jdbc:mysql://127.0.0.1:33070/employees?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );

        app.connect(url, 1000); // App uses user "employees" / pass "example"

        // Smoke check: ensure the connection is really open
        try (ResultSet ping = app.getEmployee(10001)) {
            // just try reading one row; if it throws, setup will fail here with a clear error
        }
    }

    @Test
    void testGetEmployee() throws Exception {
        try (ResultSet rs = app.getEmployee(255530)) {
            assertTrue(rs.next());
            assertEquals(255530, rs.getInt("emp_no"));
            assertEquals("Ronghao", rs.getString("first_name"));
            assertEquals("Garigliano", rs.getString("last_name"));
        }
    }
}
