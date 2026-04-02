package com.project.grievance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.grievance.model.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByGrievanceIdOrderByUploadedAtDesc(Long grievanceId);
}
