package com.napier.devops;

import com.napier.sem.App;
import com.napier.sem.Employee;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class AppTest {

    static App app;

    @BeforeAll
    static void init() {
        app = new App();
    }

    // 1) employees == null
    @Test
    void printSalariesTestNull() {
        app.printSalaries(null);   // Should NOT throw
    }

    // 2) employees is empty list
    @Test
    void printSalariesTestEmpty() {
        ArrayList<Employee> employees = new ArrayList<>();
        app.printSalaries(employees);   // Should print header only
    }

    // 3) employees list contains a null entry
    @Test
    void printSalariesTestContainsNull() {
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(null);            // Add null employee
        app.printSalaries(employees);   // Should NOT throw
    }

    // 4) employees list with real Employee data
    @Test
    void printSalariesTestNormal() {
        ArrayList<Employee> employees = new ArrayList<>();

        Employee emp = new Employee();
        emp.emp_no = 1;
        emp.first_name = "Kevin";
        emp.last_name = "Chalmers";
        emp.salary = 55000;
        employees.add(emp);

        app.printSalaries(employees);  // Should print one row
    }
}
