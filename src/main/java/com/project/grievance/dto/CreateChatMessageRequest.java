package com.project.grievance.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateChatMessageRequest {

    @NotBlank
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
