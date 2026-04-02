package com.project.grievance.service;

import com.project.grievance.dto.StatusHistoryView;
import com.project.grievance.model.GrievanceStatusHistory;

public final class GrievanceStatusHistoryMapper {

    private GrievanceStatusHistoryMapper() {}

    public static StatusHistoryView toView(GrievanceStatusHistory h) {
        StatusHistoryView v = new StatusHistoryView();
        v.setId(h.getId());
        v.setFromStatus(h.getFromStatus());
        v.setToStatus(h.getToStatus());
        v.setChangedByEmail(h.getChangedByEmail());
        v.setChangedAt(h.getChangedAt());
        v.setNote(h.getNote());
        return v;
    }
}
