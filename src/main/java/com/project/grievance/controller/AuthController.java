package com.project.grievance.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.grievance.dto.AuthRequest;
import com.project.grievance.dto.AuthResponse;
import com.project.grievance.dto.ForgotPasswordRequest;
import com.project.grievance.dto.RegisterRequest;
import com.project.grievance.dto.ResetPasswordRequest;
import com.project.grievance.model.User;
import com.project.grievance.service.AuthService;
import com.project.grievance.service.AuditLogService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AuditLogService auditLogService;

    public AuthController(AuthService authService, AuditLogService auditLogService) {
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    // 🔥 LOGIN
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody AuthRequest request, HttpServletRequest http) {
        try {
            AuthResponse response = authService.login(request);
            auditLogService.log(response.getEmail(), "LOGIN_SUCCESS", "AUTH", response.getEmail(), null, http);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            auditLogService.log(request.getEmail(), "LOGIN_FAILED", "AUTH", request.getEmail(), e.getMessage(), http);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception ex) {
            log.error("Login failed", ex);
            auditLogService.log(request.getEmail(), "LOGIN_ERROR", "AUTH", request.getEmail(), "Login failed", http);
            return ResponseEntity.status(500).body(Map.of("message", "Login failed"));
        }
    }

    // 🔥 REGISTER
    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        try {
            User saved = authService.register(request);
            auditLogService.log(saved.getEmail(), "REGISTER_SUCCESS", "AUTH", saved.getEmail(), null, http);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            auditLogService.log(request.getEmail(), "REGISTER_FAILED", "AUTH", request.getEmail(), e.getMessage(), http);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception ex) {
            log.error("Register failed", ex);
            auditLogService.log(request.getEmail(), "REGISTER_ERROR", "AUTH", request.getEmail(), "Register failed", http);
            return ResponseEntity.status(500).body(Map.of("message", "Register failed"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest http) {
        try {
            String otp = authService.forgotPassword(request);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "If the email exists, an OTP has been generated.");
            if (otp != null) {
                body.put("otp", otp);
            }
            auditLogService.log(request.getEmail(), "FORGOT_PASSWORD", "AUTH", request.getEmail(), null, http);
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            log.error("Forgot password failed", ex);
            auditLogService.log(request.getEmail(), "FORGOT_PASSWORD_ERROR", "AUTH", request.getEmail(), "Forgot password failed", http);
            return ResponseEntity.status(500).body(Map.of("message", "Forgot password failed"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest http) {
        try {
            authService.resetPassword(request);
            auditLogService.log(request.getEmail(), "RESET_PASSWORD", "AUTH", request.getEmail(), null, http);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (IllegalArgumentException e) {
            auditLogService.log(request.getEmail(), "RESET_PASSWORD_FAILED", "AUTH", request.getEmail(), e.getMessage(), http);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception ex) {
            log.error("Reset password failed", ex);
            auditLogService.log(request.getEmail(), "RESET_PASSWORD_ERROR", "AUTH", request.getEmail(), "Reset password failed", http);
            return ResponseEntity.status(500).body(Map.of("message", "Reset password failed"));
        }
    }
}