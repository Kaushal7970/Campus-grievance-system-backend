package com.project.grievance.dto;

import java.time.LocalDateTime;

import com.project.grievance.enums.EscalationLevel;

public class EscalationHistoryView {

    private Long id;
    private EscalationLevel fromLevel;
    private EscalationLevel toLevel;
    private boolean automatic;
    private String triggeredByEmail;
    private LocalDateTime triggeredAt;
    private String reason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EscalationLevel getFromLevel() {
        return fromLevel;
    }

    public void setFromLevel(EscalationLevel fromLevel) {
        this.fromLevel = fromLevel;
    }

    public EscalationLevel getToLevel() {
        return toLevel;
    }

    public void setToLevel(EscalationLevel toLevel) {
        this.toLevel = toLevel;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public String getTriggeredByEmail() {
        return triggeredByEmail;
    }

    public void setTriggeredByEmail(String triggeredByEmail) {
        this.triggeredByEmail = triggeredByEmail;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
