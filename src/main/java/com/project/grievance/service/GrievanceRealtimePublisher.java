package com.project.grievance.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GrievanceRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public GrievanceRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishGrievanceEvent(Long grievanceId, String type) {
        if (grievanceId == null || type == null || type.isBlank()) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "type", type,
                "grievanceId", grievanceId,
                "at", Instant.now().toString()
        );

        messagingTemplate.convertAndSend("/topic/grievance", payload);
        messagingTemplate.convertAndSend("/topic/grievance/" + grievanceId, payload);
    }

    public void publishGrievanceChatEvent(Long grievanceId, Long messageId) {
        if (grievanceId == null || messageId == null) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "type", "CHAT_MESSAGE",
                "grievanceId", grievanceId,
                "messageId", messageId,
                "at", Instant.now().toString()
        );

        messagingTemplate.convertAndSend("/topic/grievance/" + grievanceId + "/chat", payload);
    }
}
