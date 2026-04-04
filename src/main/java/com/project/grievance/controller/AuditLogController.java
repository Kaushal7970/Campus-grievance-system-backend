package com.project.grievance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.grievance.model.AuditLog;
import com.project.grievance.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository repo;

    @GetMapping
    public List<AuditLog> recent() {
        return repo.findTop200ByOrderByCreatedAtDesc();
    }
}
