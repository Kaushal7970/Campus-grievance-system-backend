package com.project.grievance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.project.grievance.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	List<AuditLog> findTop200ByOrderByCreatedAtDesc();
}
