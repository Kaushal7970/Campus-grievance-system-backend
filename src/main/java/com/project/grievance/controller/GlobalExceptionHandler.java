package com.project.grievance.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String msg = "Invalid request";

        if (ex != null && ex.getBindingResult() != null) {
            FieldError fe = ex.getBindingResult().getFieldError();
            if (fe != null) {
                String field = fe.getField();
                String m = fe.getDefaultMessage();
                if (m != null && !m.isBlank()) {
                    msg = m;
                } else if (field != null && !field.isBlank()) {
                    msg = "Invalid " + field;
                }
            }
        }

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(errorBody(status, msg, request));
    }

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
