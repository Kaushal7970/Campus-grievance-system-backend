package com.project.grievance.service;

import org.springframework.stereotype.Service;

import com.project.grievance.model.AuditLog;
import com.project.grievance.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(
            String actorEmail,
            String action,
            String resourceType,
            String resourceId,
            String details,
            HttpServletRequest request
    ) {
        AuditLog a = new AuditLog();
        a.setActorEmail(actorEmail == null ? "ANONYMOUS" : actorEmail);
        a.setAction(action);
        a.setResourceType(resourceType == null ? "UNKNOWN" : resourceType);
        a.setResourceId(resourceId == null ? "" : resourceId);
        a.setDetails(details);

        if (request != null) {
            a.setIp(request.getRemoteAddr());
            a.setUserAgent(request.getHeader("User-Agent"));
        }

        repository.save(a);
    }
}
