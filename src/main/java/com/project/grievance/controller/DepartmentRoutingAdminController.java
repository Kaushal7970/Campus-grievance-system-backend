package com.project.grievance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.grievance.dto.CategoryDepartmentMappingRequest;
import com.project.grievance.enums.ComplaintCategory;
import com.project.grievance.model.CategoryDepartmentMapping;
import com.project.grievance.repository.CategoryDepartmentMappingRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/routing")
@RequiredArgsConstructor
public class DepartmentRoutingAdminController {

    private final CategoryDepartmentMappingRepository repo;

    @GetMapping("/category-mappings")
    public List<CategoryDepartmentMapping> list() {
        return repo.findAll();
    }

    @PutMapping("/category-mappings/{category}")
    public CategoryDepartmentMapping upsert(
            @PathVariable String category,
            @RequestBody CategoryDepartmentMappingRequest request
    ) {
        ComplaintCategory c = ComplaintCategory.valueOf(String.valueOf(category).toUpperCase());
        if (request == null || request.getDepartment() == null) {
            throw new IllegalArgumentException("Department is required");
        }

        CategoryDepartmentMapping mapping = repo.findByCategory(c).orElseGet(CategoryDepartmentMapping::new);
        mapping.setCategory(c);
        mapping.setDepartment(request.getDepartment());
        mapping.setTargetRole(request.getTargetRole());
        return repo.save(mapping);
    }

    @DeleteMapping("/category-mappings/{category}")
    public void delete(@PathVariable String category) {
        ComplaintCategory c = ComplaintCategory.valueOf(String.valueOf(category).toUpperCase());
        repo.findByCategory(c).ifPresent(repo::delete);
    }
}
