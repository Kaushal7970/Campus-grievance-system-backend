package com.project.grievance.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.project.grievance.enums.EscalationLevel;
import com.project.grievance.model.Grievance;
import com.project.grievance.model.GrievanceEscalationHistory;
import com.project.grievance.model.GrievanceStatusHistory;
import com.project.grievance.repository.GrievanceRepository;
import com.project.grievance.repository.GrievanceEscalationHistoryRepository;
import com.project.grievance.repository.GrievanceStatusHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GrievanceService {

    private final GrievanceRepository repo;
    private final GrievanceStatusHistoryRepository statusHistoryRepository;
    private final GrievanceEscalationHistoryRepository escalationHistoryRepository;
    private final GrievanceRealtimePublisher realtimePublisher;
    private final NotificationService notificationService;

    private static final Map<EscalationLevel, String> ESCALATION_STATUS = new EnumMap<>(EscalationLevel.class);

    static {
        ESCALATION_STATUS.put(EscalationLevel.FACULTY, "ESCALATED_FACULTY");
        ESCALATION_STATUS.put(EscalationLevel.HOD, "ESCALATED_HOD");
        ESCALATION_STATUS.put(EscalationLevel.PRINCIPAL, "ESCALATED_PRINCIPAL");
        ESCALATION_STATUS.put(EscalationLevel.ADMIN, "ESCALATED_ADMIN");
    }

    public Grievance save(Grievance g) {
        boolean isNew = g.getId() == null;
        if (g.getCreatedAt() == null) {
            g.setCreatedAt(LocalDateTime.now());
        }

        if (g.getEscalationLevel() == null) {
            g.setEscalationLevel(EscalationLevel.NONE);
        }

        g.setLastUpdatedAt(LocalDateTime.now());

        // Basic SLA default: 7 days from creation if not set.
        if (g.getDeadlineAt() == null) {
            g.setDeadlineAt(g.getCreatedAt().plusDays(7));
        }

        Grievance saved = repo.save(g);

        // Generate a human-friendly complaint code after ID exists.
        if (saved.getComplaintCode() == null || saved.getComplaintCode().isBlank()) {
            String year = String.valueOf(saved.getCreatedAt().getYear());
            saved.setComplaintCode("CMP-" + year + "-" + String.format(Locale.ROOT, "%06d", saved.getId()));
            saved = repo.save(saved);
        }

        if (isNew) {
            recordStatusChange(saved, null, saved.getStatus(), saved.getStudentEmail(), "Created");
            realtimePublisher.publishGrievanceEvent(saved.getId(), "CREATED");
        } else {
            realtimePublisher.publishGrievanceEvent(saved.getId(), "UPDATED");
        }

        return saved;
    }

    public Grievance escalate(Long id, EscalationLevel toLevel, String byEmail, boolean automatic, String reason) {
        if (toLevel == null) {
            throw new IllegalArgumentException("Escalation level is required");
        }

        Grievance g = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance not found"));

        EscalationLevel fromLevel = g.getEscalationLevel() == null ? EscalationLevel.NONE : g.getEscalationLevel();
        if (toLevel.ordinal() <= fromLevel.ordinal()) {
            return g;
        }

        String fromStatus = g.getStatus();

        g.setEscalationLevel(toLevel);
        g.setLastEscalatedAt(LocalDateTime.now());
        g.setLastUpdatedAt(LocalDateTime.now());

        String escalatedStatus = ESCALATION_STATUS.get(toLevel);
        if (escalatedStatus != null && !escalatedStatus.isBlank()) {
            g.setStatus(escalatedStatus);
        }

        recordStatusChange(g, fromStatus, g.getStatus(), byEmail, reason);

        GrievanceEscalationHistory h = new GrievanceEscalationHistory();
        h.setGrievance(g);
        h.setFromLevel(fromLevel);
        h.setToLevel(toLevel);
        h.setAutomatic(automatic);
        h.setTriggeredByEmail(byEmail == null || byEmail.isBlank() ? "SYSTEM" : byEmail);
        h.setReason(reason);
        escalationHistoryRepository.save(h);

        Grievance saved = repo.save(g);
        realtimePublisher.publishGrievanceEvent(id, "ESCALATED");

        String code = saved.getComplaintCode() == null ? String.valueOf(saved.getId()) : saved.getComplaintCode();
        String msg = "Grievance " + code + " escalated to " + toLevel;

        switch (toLevel) {
            case FACULTY -> {
                // Usually requires assignment; notify HOD/Admin to act.
                notificationService.sendToRole("HOD", "ESCALATION", msg, saved.getId());
                notificationService.sendToRole("ADMIN", "ESCALATION", msg, saved.getId());
            }
            case HOD -> notificationService.sendToRole("HOD", "ESCALATION", msg, saved.getId());
            case PRINCIPAL -> {
                notificationService.sendToRole("PRINCIPAL", "ESCALATION", msg, saved.getId());
                notificationService.sendToRole("ADMIN", "ESCALATION", msg, saved.getId());
            }
            case ADMIN -> {
                notificationService.sendToRole("ADMIN", "ESCALATION", msg, saved.getId());
                notificationService.sendToRole("SUPER_ADMIN", "ESCALATION", msg, saved.getId());
            }
            default -> notificationService.send(msg);
        }

        return saved;
    }

    public EscalationLevel desiredEscalationLevelForAgeDays(long ageDays) {
        if (ageDays >= 10) return EscalationLevel.ADMIN;
        if (ageDays >= 7) return EscalationLevel.PRINCIPAL;
        if (ageDays >= 5) return EscalationLevel.HOD;
        if (ageDays >= 3) return EscalationLevel.FACULTY;
        return EscalationLevel.NONE;
    }

    public void autoEscalateIfNeeded(Grievance g) {
        if (g == null) return;

        String status = String.valueOf(g.getStatus() == null ? "" : g.getStatus()).toUpperCase(Locale.ROOT);
        if (status.equals("RESOLVED") || status.equals("CLOSED")) {
            return;
        }

        LocalDateTime createdAt = g.getCreatedAt();
        if (createdAt == null) {
            return;
        }

        long ageDays = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        EscalationLevel desired = desiredEscalationLevelForAgeDays(ageDays);

        EscalationLevel current = g.getEscalationLevel() == null ? EscalationLevel.NONE : g.getEscalationLevel();
        if (desired.ordinal() <= current.ordinal()) {
            return;
        }

        String reason = "Auto-escalated after " + ageDays + " day(s)";
        escalate(g.getId(), desired, "SYSTEM", true, reason);
    }

    public Grievance getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance not found"));
    }

    public List<Grievance> getAll() {
        return repo.findAll();
    }

    public List<Grievance> getByStudent(String email) {
        return repo.findByStudentEmail(email);
    }

    public List<Grievance> getByFaculty(String email) {
        return repo.findByAssignedTo(email);
    }

    public Grievance updateStatus(Long id, String status) {
        Grievance g = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance not found"));

        String from = g.getStatus();
        g.setStatus(status);
        g.setLastUpdatedAt(LocalDateTime.now());
        recordStatusChange(g, from, status, g.getAssignedTo() != null ? g.getAssignedTo() : g.getStudentEmail(), null);
        Grievance saved = repo.save(g);
        realtimePublisher.publishGrievanceEvent(id, "STATUS_CHANGED");
        return saved;
    }

    // 🔥 ASSIGN
    public Grievance assign(Long id, String facultyEmail) {
        Grievance g = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance not found"));

        g.setAssignedTo(facultyEmail);
        g.setLastUpdatedAt(LocalDateTime.now());
        Grievance saved = repo.save(g);
        realtimePublisher.publishGrievanceEvent(id, "ASSIGNED");
        return saved;
    }

    // 🔥 UPDATE WITH REMARKS
    public Grievance updateWithRemarks(Long id, String status, String remarks) {
        Grievance g = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance not found"));

        String from = g.getStatus();
        g.setStatus(status);
        g.setLastUpdatedAt(LocalDateTime.now());

        if (remarks != null && !remarks.isEmpty()) {
            g.setRemarks(remarks);
        }

        recordStatusChange(g, from, status, g.getAssignedTo() != null ? g.getAssignedTo() : g.getStudentEmail(), remarks);

        Grievance saved = repo.save(g);
        realtimePublisher.publishGrievanceEvent(id, "REMARKS_UPDATED");
        return saved;
    }

    private void recordStatusChange(Grievance grievance, String from, String to, String byEmail, String note) {
        if (to == null) {
            return;
        }
        GrievanceStatusHistory h = new GrievanceStatusHistory();
        h.setGrievance(grievance);
        h.setFromStatus(from == null ? "" : from);
        h.setToStatus(to);
        h.setChangedByEmail(byEmail == null ? "SYSTEM" : byEmail);
        h.setNote(note);
        statusHistoryRepository.save(h);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Grievance not found");
        }
        repo.deleteById(id);
    }

    public List<Object[]> getStats() {
        return repo.getStats();
    }
}