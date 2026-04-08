package com.project.grievance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.grievance.enums.Department;
import com.project.grievance.model.Grievance;

public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    List<Grievance> findByStudentEmail(String email);

    List<Grievance> findByStatus(String status);

    // 🔥 FACULTY
    List<Grievance> findByAssignedTo(String email);

    List<Grievance> findByDepartmentOrderByCreatedAtDesc(Department department);

    @Query("SELECT COUNT(g) FROM Grievance g WHERE LOWER(g.assignedTo) = LOWER(:email) AND UPPER(g.status) NOT IN :excluded")
    long countActiveAssignedToExcludingStatuses(
            @Param("email") String email,
            @Param("excluded") List<String> excludedStatuses
    );

    @Query("SELECT g FROM Grievance g WHERE UPPER(g.status) NOT IN :excluded")
    List<Grievance> findActiveExcludingStatuses(@Param("excluded") List<String> excludedStatuses);

    @Query("SELECT g.status, COUNT(g) FROM Grievance g GROUP BY g.status")
    List<Object[]> getStats();
}