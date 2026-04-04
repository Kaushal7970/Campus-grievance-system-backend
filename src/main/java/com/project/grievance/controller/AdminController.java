package com.project.grievance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.grievance.service.GrievanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GrievanceService service;

    // 🔥 Stats API
    @GetMapping("/stats")
    public Map<String, Long> stats() {

        List<Object[]> data = service.getStats();
        Map<String, Long> map = new HashMap<>();

        for (Object[] row : data) {
            map.put(row[0].toString(), (Long) row[1]);
        }

        return map;
    }
}