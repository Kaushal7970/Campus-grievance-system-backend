package com.project.grievance.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.grievance.model.Notification;
import com.project.grievance.repository.NotificationRepository;
import com.project.grievance.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final NotificationRepository repo;

    private static String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    private static boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority a : auth.getAuthorities()) {
            String v = a.getAuthority();
            for (String r : roles) {
                if (v.equalsIgnoreCase("ROLE_" + r)) return true;
            }
        }
        return false;
    }

    @GetMapping
    public List<Notification> listMine() {
        String email = currentEmail();
        if (email == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return repo.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount() {
        String email = currentEmail();
        if (email == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        long count = repo.countByRecipientEmailAndReadAtIsNull(email);
        return Map.of("count", count);
    }

    @PutMapping("/{id}/read")
    public Notification markRead(@PathVariable Long id) {
        String email = currentEmail();
        Notification n = repo.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));

        // Owner or admin/super-admin can mark as read.
        if (email == null || (!email.equalsIgnoreCase(n.getRecipientEmail()) && !hasAnyRole("ADMIN", "SUPER_ADMIN"))) {
            throw new IllegalArgumentException("Access denied");
        }

        if (n.getReadAt() == null) {
            n.setReadAt(LocalDateTime.now());
            n = repo.save(n);
        }
        return n;
    }

    // Admin utility: send a notification to a user or role
    @PostMapping
    public void send(
            @RequestParam String message,
            @RequestParam(required = false) String recipientEmail,
            @RequestParam(required = false) String role,
            @RequestParam(required = false, defaultValue = "INFO") String type,
            @RequestParam(required = false) Long grievanceId
    ) {
        if (!hasAnyRole("ADMIN", "SUPER_ADMIN")) {
            throw new IllegalArgumentException("Access denied");
        }

        if (recipientEmail != null && !recipientEmail.isBlank()) {
            service.sendToUser(recipientEmail, type, message, grievanceId);
            return;
        }

        if (role != null && !role.isBlank()) {
            service.sendToRole(role, type, message, grievanceId);
            return;
        }

        service.send(message);
    }
}