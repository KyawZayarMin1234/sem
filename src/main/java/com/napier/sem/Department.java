package com.napier.sem;

/**
 * Simple Department DTO.
 */
public final class Department {

    /** Department numeric id (or code mapped to an int). */
    private int deptNo;

    /** Department name. */
    private String deptName;

    /** Manager full name (already composed), or {@code "N/A"}. */
    private String manager;

    /** @return department id */
    public int getDeptNo() {
        return deptNo;
    }

    /**
     * Set the department id.
     * @param newDeptNo the id to set
     */
    public void setDeptNo(final int newDeptNo) {
        this.deptNo = newDeptNo;
    }

    /** @return department name */
    public String getDeptName() {
        return deptName;
    }

    /**
     * Set the department name.
     * @param newDeptName name to set
     */
    public void setDeptName(final String newDeptName) {
        this.deptName = newDeptName;
    }

    /** @return manager full name or "N/A" */
    public String getManager() {
        return manager;
    }

    /**
     * Set manager full name.
     * @param newManager manager name (or "N/A")
     */
    public void setManager(final String newManager) {
        this.manager = newManager;
    }

    @Override
    public String toString() {
        return deptName + " (" + deptNo + ")";
    }
}
