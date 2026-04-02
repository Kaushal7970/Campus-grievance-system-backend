package com.project.grievance.dto;

import jakarta.validation.constraints.NotBlank;

public class AnnouncementCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private String audienceRole;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAudienceRole() {
        return audienceRole;
    }

    public void setAudienceRole(String audienceRole) {
        this.audienceRole = audienceRole;
    }
}
