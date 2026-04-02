package com.project.grievance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.grievance.model.GrievanceComment;

public interface GrievanceCommentRepository extends JpaRepository<GrievanceComment, Long> {

    List<GrievanceComment> findByGrievanceIdOrderByCreatedAtAsc(Long grievanceId);
}
