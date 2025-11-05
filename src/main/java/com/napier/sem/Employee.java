package com.napier.sem;

/** Represents an employee */
public class Employee {
    public int emp_no;
    public String first_name;
    public String last_name;

    // extra fields for the exercise
    public String title;     // current job title
    public int salary;       // current salary
    public Department dept; // current department
    public Employee manager;   // current manager full name

    @Override
    public String toString() {
        return String.format(
                "%d %s %s%n%s%nSalary: %d%n%s%nManager: %s%n",
                emp_no,
                first_name,
                last_name,
                (title == null ? "N/A" : title),
                salary,
                (dept == null ? "N/A" : dept),
                (manager == null ? "N/A" : (manager.first_name + " " + manager.last_name))
        );
    }

}
