package com.project.grievance.dto;

import lombok.Data;

@Data
public class GrievanceDTO {
    private String title;
    private String description;
    private Boolean isAnonymous;
}