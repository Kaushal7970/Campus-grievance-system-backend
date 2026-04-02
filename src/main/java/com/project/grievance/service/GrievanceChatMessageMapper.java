package com.project.grievance.service;

import com.project.grievance.dto.ChatMessageView;
import com.project.grievance.model.GrievanceChatMessage;

public final class GrievanceChatMessageMapper {

    private GrievanceChatMessageMapper() {}

    public static ChatMessageView toView(GrievanceChatMessage m) {
        ChatMessageView v = new ChatMessageView();
        v.setId(m.getId());
        v.setSenderEmail(m.getSenderEmail());
        v.setMessage(m.getMessage());
        v.setCreatedAt(m.getCreatedAt());
        return v;
    }
}
