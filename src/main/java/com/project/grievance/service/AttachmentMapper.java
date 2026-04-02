package com.project.grievance.service;

import com.project.grievance.dto.AttachmentView;
import com.project.grievance.model.Attachment;

public final class AttachmentMapper {

    private AttachmentMapper() {}

    public static AttachmentView toView(Attachment a) {
        AttachmentView v = new AttachmentView();
        v.setId(a.getId());
        v.setOriginalFileName(a.getOriginalFileName());
        v.setContentType(a.getContentType());
        v.setSizeBytes(a.getSizeBytes());
        v.setUploadedByEmail(a.getUploadedByEmail());
        v.setUploadedAt(a.getUploadedAt());
        return v;
    }
}
