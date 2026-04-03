package com.project.grievance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String role; // ADMIN / FACULTY / STUDENT

    // --- Security hardening ---
    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    private Instant lockedUntil;

    private Instant lastLoginAt;

    // 🔹 Constructors
    public User() {}

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // 🔹 Getters & Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmailValue(email);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return normalizeRoleValue(role);
    }

    public void setRole(String role) {
        this.role = normalizeRoleValue(role);
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean normalizeStoredFields() {
        String normalizedEmail = normalizeEmailValue(this.email);
        String normalizedRole = normalizeRoleValue(this.role);
        boolean changed = !Objects.equals(this.email, normalizedEmail)
                || !Objects.equals(this.role, normalizedRole);
        this.email = normalizedEmail;
        this.role = normalizedRole;
        return changed;
    }

    @PrePersist
    @PreUpdate
    public void preSaveNormalize() {
        normalizeStoredFields();
    }

    private static String normalizeEmailValue(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private static String normalizeRoleValue(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        while (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized.isBlank() ? null : normalized;
    }
}
