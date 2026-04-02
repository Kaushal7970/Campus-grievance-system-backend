package com.project.grievance.repository;

import com.project.grievance.model.Grievance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    List<Grievance> findByStudentEmail(String email);

    List<Grievance> findByStatus(String status);

    // 🔥 FACULTY
    List<Grievance> findByAssignedTo(String email);

    @Query("SELECT g FROM Grievance g WHERE UPPER(g.status) NOT IN :excluded")
    List<Grievance> findActiveExcludingStatuses(@Param("excluded") List<String> excludedStatuses);

    @Query("SELECT g.status, COUNT(g) FROM Grievance g GROUP BY g.status")
    List<Object[]> getStats();
}