package com.project.grievance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.grievance.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);

	long countByRecipientEmailAndReadAtIsNull(String recipientEmail);

	void deleteByGrievanceId(Long grievanceId);
}