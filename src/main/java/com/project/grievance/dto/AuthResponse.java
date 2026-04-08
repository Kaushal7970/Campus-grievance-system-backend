package com.project.grievance.dto;

public class AuthResponse {

    private Long id;
    private String token;
    private String role;
    private String email;
    private String phoneNumber;
    private String name;
    private String department;
    private String avatarUrl;
    private boolean smsNotificationsEnabled;
    private boolean emailNotificationsEnabled;
    private String themeId;

    public AuthResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isSmsNotificationsEnabled() { return smsNotificationsEnabled; }
    public void setSmsNotificationsEnabled(boolean smsNotificationsEnabled) { this.smsNotificationsEnabled = smsNotificationsEnabled; }

    public boolean isEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }

    public String getThemeId() { return themeId; }
    public void setThemeId(String themeId) { this.themeId = themeId; }
}