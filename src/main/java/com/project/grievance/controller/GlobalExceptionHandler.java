package com.project.grievance.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String msg = ex.getMessage() == null ? "Invalid request" : ex.getMessage();

        HttpStatus status;
        if (msg.equalsIgnoreCase("unauthorized")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (msg.equalsIgnoreCase("access denied")) {
            status = HttpStatus.FORBIDDEN;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status).body(errorBody(status, msg, request));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        String msg = ex.getMessage() == null ? "Internal Server Error" : ex.getMessage();

        // Narrow mapping: only obvious "not found" cases become 404; everything else stays 500.
        HttpStatus status = msg.toLowerCase().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
        String safeMessage = status == HttpStatus.INTERNAL_SERVER_ERROR ? "Internal Server Error" : msg;

        return ResponseEntity.status(status).body(errorBody(status, safeMessage, request));
    }

    private static Map<String, Object> errorBody(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request == null ? null : request.getRequestURI());
        return body;
    }
}
