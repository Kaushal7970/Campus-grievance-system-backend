package com.project.grievance.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.grievance.service.AiAdminRulesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/ai-rules")
@RequiredArgsConstructor
public class AdminAiRulesController {

    private final AiAdminRulesService aiAdminRulesService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getRules() {
        return ResponseEntity.ok(aiAdminRulesService.getRulesView());
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateRules(
            Authentication authentication,
            @RequestBody Map<String, Object> body
    ) {
        String rules = body == null ? "" : String.valueOf(body.getOrDefault("rules", ""));
        String email = authentication == null ? "" : String.valueOf(authentication.getName());
        return ResponseEntity.ok(aiAdminRulesService.updateRules(rules, email));
    }
}
