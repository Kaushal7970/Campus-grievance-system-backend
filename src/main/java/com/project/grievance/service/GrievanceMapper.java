package com.project.grievance.service;

import com.project.grievance.dto.GrievanceView;
import com.project.grievance.model.Grievance;

public final class GrievanceMapper {

    private GrievanceMapper() {}

    public static GrievanceView toView(Grievance grievance) {
        GrievanceView v = new GrievanceView();
        v.setId(grievance.getId());
        v.setComplaintCode(grievance.getComplaintCode());
        v.setTitle(grievance.getTitle());
        v.setDescription(grievance.getDescription());
        v.setStatus(grievance.getStatus());
        v.setPriority(grievance.getPriority());
        v.setCategory(grievance.getCategory());
        v.setDepartment(grievance.getDepartment());
        v.setAnonymous(grievance.isAnonymous());
        v.setStudentEmail(grievance.getStudentEmail());
        v.setAssignedTo(grievance.getAssignedTo());
        v.setRemarks(grievance.getRemarks());
        v.setCreatedAt(grievance.getCreatedAt());
        v.setLastUpdatedAt(grievance.getLastUpdatedAt());
        v.setDeadlineAt(grievance.getDeadlineAt());
        v.setEscalationLevel(grievance.getEscalationLevel());
        v.setLastEscalatedAt(grievance.getLastEscalatedAt());
        return v;
    }
}
