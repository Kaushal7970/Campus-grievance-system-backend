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

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/auth", "/auth"})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 🔥 LOGIN
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception ex) {
            log.error("Login failed", ex);
            return ResponseEntity.status(500).body(Map.of("message", "Login failed"));
        }
    }

    // 🔥 REGISTER
    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User saved = authService.register(request);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception ex) {
            log.error("Register failed", ex);
            return ResponseEntity.status(500).body(Map.of("message", "Register failed"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String otp = authService.forgotPassword(request);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "If the email exists, an OTP has been generated.");
            if (otp != null) {
                body.put("otp", otp);
            }
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            log.error("Forgot password failed", ex);
            return ResponseEntity.status(500).body(Map.of("message", "Forgot password failed"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception ex) {
            log.error("Reset password failed", ex);
            return ResponseEntity.status(500).body(Map.of("message", "Reset password failed"));
        }
    }
}