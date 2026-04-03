package com.project.grievance.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicDebugController {

    @GetMapping("/debug")
    public Map<String, Object> debug(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth == null ? null : auth.getName();

        boolean hasAuthHeader = authorization != null && !authorization.isBlank();
        int authHeaderLength = hasAuthHeader ? authorization.length() : 0;
        String authHeaderPrefix = hasAuthHeader
                ? authorization.substring(0, Math.min(authorization.length(), 12))
                : null;

        return Map.of(
                "hasAuthorizationHeader", hasAuthHeader,
                "authorizationHeaderLength", authHeaderLength,
                "authorizationHeaderPrefix", authHeaderPrefix,
                "securityContextName", name
        );
    }
}
