package com.project.grievance.model;

import java.util.Locale;

import com.project.grievance.enums.ComplaintCategory;
import com.project.grievance.enums.Department;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "category_department_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category"})
)
public class CategoryDepartmentMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    // Optional: constrain assignment to a specific role for this mapping (e.g., HOD, PRINCIPAL).
    private String targetRole;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getTargetRole() {
        return normalizeRoleValue(targetRole);
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = normalizeRoleValue(targetRole);
    }

    private static String normalizeRoleValue(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        while (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized.isBlank() ? null : normalized;
    }
}
