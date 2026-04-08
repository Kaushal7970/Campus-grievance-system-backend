package com.project.grievance.model;

import java.time.LocalDateTime;

import com.project.grievance.enums.ComplaintCategory;
import com.project.grievance.enums.Department;
import com.project.grievance.enums.EscalationLevel;
import com.project.grievance.enums.Priority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "grievance")
public class Grievance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(unique = true)
    private String complaintCode;

    private String status = "PENDING";

    @Enumerated(EnumType.STRING)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    private Department department;

    private String studentEmail;

    // 🔥 NEW FIELD: ANONYMOUS
    private boolean anonymous = false;

    // 🔥 ASSIGNED FACULTY
    private String assignedTo;

    // 🔥 REMARKS (solution)
    @Column(length = 1000)
    private String remarks;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.LOW;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    private LocalDateTime deadlineAt;

    @Enumerated(EnumType.STRING)
    private EscalationLevel escalationLevel = EscalationLevel.NONE;

    private LocalDateTime lastEscalatedAt;

    // ===== GETTERS / SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getComplaintCode() { return complaintCode; }
    public void setComplaintCode(String complaintCode) { this.complaintCode = complaintCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    // 🔥 ANONYMOUS GETTER/SETTER
    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }

    public EscalationLevel getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(EscalationLevel escalationLevel) { this.escalationLevel = escalationLevel; }

    public LocalDateTime getLastEscalatedAt() { return lastEscalatedAt; }
    public void setLastEscalatedAt(LocalDateTime lastEscalatedAt) { this.lastEscalatedAt = lastEscalatedAt; }
}