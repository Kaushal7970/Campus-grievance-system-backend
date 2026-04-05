package com.project.grievance.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/api/public", "/public" })
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "timestamp", Instant.now().toString()
        ));
    }
}
