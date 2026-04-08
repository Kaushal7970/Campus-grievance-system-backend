package com.project.grievance.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.grievance.enums.Department;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

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

    @Enumerated(EnumType.STRING)
    private Department department;

    @Column(length = 20)
    private String phoneNumber; // E.164 preferred, e.g. +91XXXXXXXXXX

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 700)
    @JsonIgnore
    private String avatarStoragePath;

    @Column(nullable = false)
    private boolean smsNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean emailNotificationsEnabled = true;

    @Column(length = 32)
    private String themeId;

    @Column(nullable = false)
    private int tokenVersion = 0;

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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = normalizePhoneNumberValue(phoneNumber);
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = normalizeNullableTrimmed(avatarUrl);
    }

    public String getAvatarStoragePath() {
        return avatarStoragePath;
    }

    public void setAvatarStoragePath(String avatarStoragePath) {
        this.avatarStoragePath = normalizeNullableTrimmed(avatarStoragePath);
    }

    public boolean isSmsNotificationsEnabled() {
        return smsNotificationsEnabled;
    }

    public void setSmsNotificationsEnabled(boolean smsNotificationsEnabled) {
        this.smsNotificationsEnabled = smsNotificationsEnabled;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        String v = normalizeNullableTrimmed(themeId);
        this.themeId = v == null ? null : v.toLowerCase(Locale.ROOT);
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = Math.max(0, tokenVersion);
    }

    public boolean normalizeStoredFields() {
        String normalizedEmail = normalizeEmailValue(this.email);
        String normalizedRole = normalizeRoleValue(this.role);
        String normalizedPhone = normalizePhoneNumberValue(this.phoneNumber);
        String normalizedAvatarUrl = normalizeNullableTrimmed(this.avatarUrl);
        String normalizedAvatarPath = normalizeNullableTrimmed(this.avatarStoragePath);
        String normalizedThemeId = normalizeNullableTrimmed(this.themeId);
        if (normalizedThemeId != null) normalizedThemeId = normalizedThemeId.toLowerCase(Locale.ROOT);
        boolean changed = !Objects.equals(this.email, normalizedEmail)
            || !Objects.equals(this.role, normalizedRole)
            || !Objects.equals(this.phoneNumber, normalizedPhone)
            || !Objects.equals(this.avatarUrl, normalizedAvatarUrl)
            || !Objects.equals(this.avatarStoragePath, normalizedAvatarPath)
            || !Objects.equals(this.themeId, normalizedThemeId);
        this.email = normalizedEmail;
        this.role = normalizedRole;
        this.phoneNumber = normalizedPhone;
        this.avatarUrl = normalizedAvatarUrl;
        this.avatarStoragePath = normalizedAvatarPath;
        this.themeId = normalizedThemeId;
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

    private static String normalizePhoneNumberValue(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        String v = phoneNumber.trim();
        if (v.isBlank()) {
            return null;
        }

        // Strip common separators
        v = v.replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");

        // Already E.164 (+<country><number>)
        if (v.startsWith("+")) {
            String digits = v.substring(1);
            if (!digits.chars().allMatch(Character::isDigit)) {
                throw new IllegalArgumentException("Invalid phone number. Use +91XXXXXXXXXX");
            }
            if (digits.length() < 10 || digits.length() > 15) {
                throw new IllegalArgumentException("Invalid phone number length. Use +91XXXXXXXXXX");
            }
            return "+" + digits;
        }

        // Digits-only inputs
        if (!v.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Invalid phone number. Use +91XXXXXXXXXX");
        }

        // India-friendly normalization
        if (v.length() == 10) {
            return "+91" + v;
        }
        if (v.length() == 11 && v.startsWith("0")) {
            return "+91" + v.substring(1);
        }
        if (v.length() == 12 && v.startsWith("91")) {
            return "+" + v;
        }

        throw new IllegalArgumentException("Invalid phone number. Use +91XXXXXXXXXX");
    }

    private static String normalizeNullableTrimmed(String v) {
        if (v == null) return null;
        String s = v.trim();
        return s.isBlank() ? null : s;
    }
}
