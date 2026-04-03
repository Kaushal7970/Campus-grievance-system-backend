package com.project.grievance.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "ok", true,
                "ts", Instant.now().toString()
        );
    }
}
