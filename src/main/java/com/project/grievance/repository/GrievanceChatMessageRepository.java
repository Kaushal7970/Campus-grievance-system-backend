package com.project.grievance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.grievance.model.GrievanceChatMessage;

public interface GrievanceChatMessageRepository extends JpaRepository<GrievanceChatMessage, Long> {

    List<GrievanceChatMessage> findByGrievanceIdOrderByCreatedAtAsc(Long grievanceId);

    void deleteByGrievanceId(Long grievanceId);
}
