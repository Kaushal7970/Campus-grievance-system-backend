package com.project.grievance.service;

import com.project.grievance.dto.EscalationHistoryView;
import com.project.grievance.model.GrievanceEscalationHistory;

public final class GrievanceEscalationHistoryMapper {

    private GrievanceEscalationHistoryMapper() {}

    public static EscalationHistoryView toView(GrievanceEscalationHistory h) {
        EscalationHistoryView v = new EscalationHistoryView();
        v.setId(h.getId());
        v.setFromLevel(h.getFromLevel());
        v.setToLevel(h.getToLevel());
        v.setAutomatic(h.isAutomatic());
        v.setTriggeredByEmail(h.getTriggeredByEmail());
        v.setTriggeredAt(h.getTriggeredAt());
        v.setReason(h.getReason());
        return v;
    }
}
