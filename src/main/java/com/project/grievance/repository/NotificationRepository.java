package com.project.grievance.repository;

import com.project.grievance.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);

	long countByRecipientEmailAndReadAtIsNull(String recipientEmail);
}