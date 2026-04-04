package com.project.grievance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.grievance.model.GrievanceStatusHistory;

public interface GrievanceStatusHistoryRepository extends JpaRepository<GrievanceStatusHistory, Long> {

    List<GrievanceStatusHistory> findByGrievanceIdOrderByChangedAtAsc(Long grievanceId);

    void deleteByGrievanceId(Long grievanceId);
}
