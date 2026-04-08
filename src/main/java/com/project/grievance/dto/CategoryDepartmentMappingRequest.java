package com.project.grievance.dto;

import com.project.grievance.enums.Department;

public class CategoryDepartmentMappingRequest {

    private Department department;
    private String targetRole;

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
}
