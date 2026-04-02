package com.project.grievance.service;

import com.project.grievance.model.Notification;
import com.project.grievance.repository.NotificationRepository;
import com.project.grievance.repository.UserRepository;
import com.project.grievance.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final UserRepository userRepository;
    private final NotificationRealtimePublisher realtimePublisher;

    public void send(String message) {
        // Backwards-compatible behavior: notify all admins.
        sendToRole("ADMIN", "INFO", message, null);
        sendToRole("SUPER_ADMIN", "INFO", message, null);
    }

    public Notification sendToUser(String recipientEmail, String type, String message, Long grievanceId) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException("recipientEmail is required");
        }
        Notification n = new Notification();
        n.setRecipientEmail(recipientEmail);
        n.setType(type);
        n.setGrievanceId(grievanceId);
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        Notification saved = repo.save(n);
        realtimePublisher.publishNotificationEvent(recipientEmail, saved.getId());
        return saved;
    }

    public void sendToRole(String role, String type, String message, Long grievanceId) {
        if (role == null || role.isBlank()) {
            return;
        }
        List<User> users = userRepository.findByRoleIgnoreCase(role);
        for (User u : users) {
            sendToUser(u.getEmail(), type, message, grievanceId);
        }
    }
}