package com.project.grievance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicAuthDebugController {

    @GetMapping("/whoami")
    public Map<String, Object> whoami() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Map.of(
                    "authenticated", false,
                    "name", null,
                    "authorities", List.of()
            );
        }

        List<String> authorities = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Map.of(
                "authenticated", auth.isAuthenticated(),
                "name", auth.getName(),
                "authorities", authorities
        );
    }
}
