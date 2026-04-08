package com.project.grievance.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.project.grievance.enums.EscalationLevel;
import com.project.grievance.model.Grievance;
import com.project.grievance.model.User;
import com.project.grievance.repository.UserRepository;
import com.project.grievance.sms.SmsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmsNotificationService {

    private final SmsService smsService;
    private final UserRepository userRepository;

    public void onComplaintSubmitted(Grievance g) {
        if (g == null) return;
        String code = complaintCodeOrId(g);
        String msg = "Complaint " + code + " submitted. We'll update you.";
        smsService.sendSafe(phoneOfUserEmail(g.getStudentEmail()), msg);
    }

    public void onComplaintAssigned(Grievance g) {
        if (g == null) return;

        String code = complaintCodeOrId(g);
        String dept = g.getDepartment() == null ? null : String.valueOf(g.getDepartment()).toUpperCase(Locale.ROOT);

        // Student message
        String studentMsg = dept == null
                ? ("Complaint " + code + " assigned.")
                : ("Complaint " + code + " assigned to " + dept + ".");
        smsService.sendSafe(phoneOfUserEmail(g.getStudentEmail()), studentMsg);

        // Staff message
        String staffMsg = "New complaint assigned: " + code;
        smsService.sendSafe(phoneOfUserEmail(g.getAssignedTo()), staffMsg);
    }

    public void onStatusUpdated(Grievance g, String status) {
        if (g == null) return;
        String code = complaintCodeOrId(g);
        String s = status == null ? "UPDATED" : status.trim().toUpperCase(Locale.ROOT);
        String msg = "Complaint " + code + " status: " + s + ".";
        smsService.sendSafe(phoneOfUserEmail(g.getStudentEmail()), msg);
    }

    public void onEscalated(Grievance g, EscalationLevel to) {
        if (g == null || to == null) return;
        String code = complaintCodeOrId(g);
        String msg = "Complaint " + code + " escalated to " + to + ".";
        smsService.sendSafe(phoneOfUserEmail(g.getStudentEmail()), msg);
    }

    private String phoneOfUserEmail(String email) {
        if (email == null || email.isBlank()) return null;
        User u = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT)).orElse(null);
        if (u == null) return null;
        if (!u.isSmsNotificationsEnabled()) return null;
        return u.getPhoneNumber();
    }

    private static String complaintCodeOrId(Grievance g) {
        if (g == null) return "";
        if (g.getComplaintCode() != null && !g.getComplaintCode().isBlank()) return g.getComplaintCode();
        return g.getId() == null ? "" : String.valueOf(g.getId());
    }
}
