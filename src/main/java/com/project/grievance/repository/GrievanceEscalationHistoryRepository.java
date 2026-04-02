package com.project.grievance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.grievance.model.GrievanceEscalationHistory;

public interface GrievanceEscalationHistoryRepository extends JpaRepository<GrievanceEscalationHistory, Long> {

    List<GrievanceEscalationHistory> findByGrievanceIdOrderByTriggeredAtAsc(Long grievanceId);
}
