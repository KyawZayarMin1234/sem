package com.napier.sem;

/**
 * Simple Employee DTO.
 */
public final class Employee {

    /** Employee number. */
    private int empNo;

    /** First name. */
    private String firstName;

    /** Last name. */
    private String lastName;

    /** Current job title. */
    private String title;

    /** Current salary. */
    private int salary;

    /** Current department. */
    private Department dept;

    /** Current manager (as an Employee object). */
    private Employee manager;

    /** @return employee number */
    public int getEmpNo() {
        return empNo;
    }

    /**
     * Set employee number.
     * @param newEmpNo value to set
     */
    public void setEmpNo(final int newEmpNo) {
        this.empNo = newEmpNo;
    }

    /** @return first name */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set first name.
     * @param newFirstName value to set
     */
    public void setFirstName(final String newFirstName) {
        this.firstName = newFirstName;
    }

    /** @return last name */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set last name.
     * @param newLastName value to set
     */
    public void setLastName(final String newLastName) {
        this.lastName = newLastName;
    }

    /** @return current title (may be {@code null}) */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     * @param newTitle value to set
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
    }

    /** @return current salary */
    public int getSalary() {
        return salary;
    }

    /**
     * Set salary.
     * @param newSalary value to set
     */
    public void setSalary(final int newSalary) {
        this.salary = newSalary;
    }

    /** @return department (may be {@code null}) */
    public Department getDept() {
        return dept;
    }

    /**
     * Set department.
     * @param newDept value to set
     */
    public void setDept(final Department newDept) {
        this.dept = newDept;
    }

    /** @return manager (may be {@code null}) */
    public Employee getManager() {
        return manager;
    }

    /**
     * Set manager.
     * @param newManager value to set
     */
    public void setManager(final Employee newManager) {
        this.manager = newManager;
    }

    @Override
    public String toString() {
        final String first =
                firstName == null ? "" : firstName;
        final String last =
                lastName == null ? "" : lastName;
        final String t =
                title == null ? "N/A" : title;
        final String d =
                (dept == null) ? "N/A" : dept.toString();
        final String mgr;
        if (manager == null) {
            mgr = "N/A";
        } else {
            final String mf =
                    manager.firstName == null ? "" : manager.firstName;
            final String ml =
                    manager.lastName == null ? "" : manager.lastName;
            mgr = (mf + " " + ml).trim();
        }

        return String.format(
                "%d %s %s%n%s%nSalary: %d%n%s%nManager: %s%n",
                empNo, first, last, t, salary, d, mgr
        );
    }
}
