package com.project.grievance.model;

import java.time.LocalDateTime;

import com.project.grievance.enums.EscalationLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "grievance_escalation_history", indexes = {
        @Index(name = "idx_grievance_escalation_grievance", columnList = "grievance_id")
})
public class GrievanceEscalationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grievance_id", nullable = false)
    private Grievance grievance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscalationLevel fromLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscalationLevel toLevel;

    @Column(nullable = false)
    private boolean automatic;

    @Column(nullable = false)
    private String triggeredByEmail;

    @Column(nullable = false)
    private LocalDateTime triggeredAt = LocalDateTime.now();

    @Column(length = 1000)
    private String reason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Grievance getGrievance() {
        return grievance;
    }

    public void setGrievance(Grievance grievance) {
        this.grievance = grievance;
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
