package com.napier.devops;

import com.napier.sem.App;
import org.junit.jupiter.api.*;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppIntegrationTest {

    private App app;

    @BeforeAll
    void init() {
        app = new App();
        // Your compose maps host 35432 -> container 3306
        String url = "jdbc:mysql://127.0.0.1:35432/employees"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        // 1s retry delay inside App.connect
        app.connect(url, 1000);
    }

    @AfterAll
    void cleanup() {
        app.disconnect();
    }

    @Test
    void testGetEmployee() throws Exception {
        try (ResultSet rs = app.getEmployee(255530)) {
            assertTrue(rs.next(), "No row returned for emp_no 255530");
            assertEquals(255530, rs.getInt("emp_no"));
            assertEquals("Ronghao", rs.getString("first_name"));
            assertEquals("Garigliano", rs.getString("last_name"));
        }
    }
}
