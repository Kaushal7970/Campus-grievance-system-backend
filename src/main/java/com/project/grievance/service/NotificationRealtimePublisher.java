package com.project.grievance.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishNotificationEvent(String recipientEmail, Long notificationId) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "type", "NOTIFICATION",
                "notificationId", notificationId,
                "recipientEmail", recipientEmail,
                "at", Instant.now().toString()
        );

        messagingTemplate.convertAndSend("/topic/notifications/" + recipientEmail, payload);
    }
}
