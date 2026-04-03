package com.project.grievance.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/ping")
    public Map<String, Object> ping(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String name = auth == null ? null : auth.getName();
    List<String> authorities = auth == null
        ? List.of()
        : auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    boolean hasAuthHeader = authorization != null && !authorization.isBlank();
    String authHeaderPrefix = hasAuthHeader
        ? authorization.substring(0, Math.min(authorization.length(), 12))
        : null;

    return Map.of(
        "ok", true,
        "ts", Instant.now().toString(),
        "hasAuthorizationHeader", hasAuthHeader,
        "authorizationHeaderPrefix", authHeaderPrefix,
        "securityContextName", name,
        "authorities", authorities
    );
    }
}
