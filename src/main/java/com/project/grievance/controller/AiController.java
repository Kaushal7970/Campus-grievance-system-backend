package com.project.grievance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.grievance.dto.AiBotRequest;
import com.project.grievance.dto.AiBotResponse;
import com.project.grievance.dto.AiChatRequest;
import com.project.grievance.dto.AiChatResponse;
import com.project.grievance.service.AiChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiChatService aiChatService;

    public AiController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiChatService.chat(request.getMessage()));
    }

    @PostMapping("/bot")
    public ResponseEntity<AiBotResponse> bot(@Valid @RequestBody AiBotRequest request) {
        return ResponseEntity.ok(aiChatService.botChat(request.getMessage()));
    }

    @GetMapping("/status")
    public ResponseEntity<Object> status() {
        return ResponseEntity.ok(aiChatService.status());
    }
}
