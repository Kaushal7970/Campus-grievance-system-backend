package com.project.grievance.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public AnnouncementRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishAnnouncementEvent(String action, Long announcementId) {
        Map<String, Object> payload = Map.of(
                "type", "ANNOUNCEMENT",
                "action", action,
                "announcementId", announcementId,
                "at", Instant.now().toString()
        );

        messagingTemplate.convertAndSend("/topic/announcements", payload);
    }
}
