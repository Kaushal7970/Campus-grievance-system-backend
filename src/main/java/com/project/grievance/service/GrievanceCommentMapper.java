package com.project.grievance.service;

import com.project.grievance.dto.CommentView;
import com.project.grievance.model.GrievanceComment;

public final class GrievanceCommentMapper {

    private GrievanceCommentMapper() {}

    public static CommentView toView(GrievanceComment c) {
        CommentView v = new CommentView();
        v.setId(c.getId());
        v.setAuthorEmail(c.getAuthorEmail());
        v.setMessage(c.getMessage());
        v.setInternalOnly(c.isInternalOnly());
        v.setCreatedAt(c.getCreatedAt());
        return v;
    }
}
