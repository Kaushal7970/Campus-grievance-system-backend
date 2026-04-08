package com.project.grievance.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.project.grievance.enums.ComplaintCategory;
import com.project.grievance.enums.Department;
import com.project.grievance.model.CategoryDepartmentMapping;
import com.project.grievance.repository.CategoryDepartmentMappingRepository;

@Component
public class CategoryDepartmentMappingSeeder implements CommandLineRunner {

    private final CategoryDepartmentMappingRepository repo;

    public CategoryDepartmentMappingSeeder(CategoryDepartmentMappingRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        // Seed defaults only when a mapping for that category doesn't exist.
        seedIfMissing(ComplaintCategory.HOSTEL, Department.HOSTEL, "WARDEN");
        seedIfMissing(ComplaintCategory.INFRASTRUCTURE, Department.INFRASTRUCTURE, null);
        seedIfMissing(ComplaintCategory.ELECTRICITY, Department.ELECTRICITY, null);
        seedIfMissing(ComplaintCategory.ACADEMIC, Department.ACADEMIC, "HOD");
        seedIfMissing(ComplaintCategory.FACULTY_BEHAVIOR, Department.ADMINISTRATION, "PRINCIPAL");
        seedIfMissing(ComplaintCategory.ADMINISTRATION, Department.ADMINISTRATION, "ADMIN");
    }

    private void seedIfMissing(ComplaintCategory category, Department department, String targetRole) {
        repo.findByCategory(category).ifPresentOrElse(
                existing -> {
                    // don't override admin configuration
                },
                () -> {
                    CategoryDepartmentMapping m = new CategoryDepartmentMapping();
                    m.setCategory(category);
                    m.setDepartment(department);
                    m.setTargetRole(targetRole);
                    repo.save(m);
                }
        );
    }
}
