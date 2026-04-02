package com.project.grievance.enums;

public enum Status {
    // Legacy / initial
    PENDING,

    // Spec lifecycle
    SUBMITTED,
    UNDER_REVIEW,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    REOPENED,

    // Escalations
    ESCALATED,
    ESCALATED_HOD,
    ESCALATED_ADMIN
}