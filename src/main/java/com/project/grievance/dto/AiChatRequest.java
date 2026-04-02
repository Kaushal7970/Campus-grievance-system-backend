package com.project.grievance.dto;

import jakarta.validation.constraints.NotBlank;

public class AiChatRequest {

    @NotBlank
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
