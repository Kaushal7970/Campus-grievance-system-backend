package com.project.grievance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.grievance.enums.ComplaintCategory;
import com.project.grievance.model.CategoryDepartmentMapping;

public interface CategoryDepartmentMappingRepository extends JpaRepository<CategoryDepartmentMapping, Long> {
    Optional<CategoryDepartmentMapping> findByCategory(ComplaintCategory category);
}
