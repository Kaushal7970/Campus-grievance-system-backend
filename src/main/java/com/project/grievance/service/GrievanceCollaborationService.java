package com.project.grievance.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project.grievance.model.Attachment;
import com.project.grievance.model.Grievance;
import com.project.grievance.model.GrievanceChatMessage;
import com.project.grievance.model.GrievanceComment;
import com.project.grievance.repository.AttachmentRepository;
import com.project.grievance.repository.GrievanceChatMessageRepository;
import com.project.grievance.repository.GrievanceCommentRepository;
import com.project.grievance.repository.GrievanceRepository;

@Service
public class GrievanceCollaborationService {

    private final GrievanceRepository grievanceRepository;
    private final GrievanceCommentRepository commentRepository;
    private final GrievanceChatMessageRepository chatRepository;
    private final AttachmentRepository attachmentRepository;
    private final GrievanceRealtimePublisher realtimePublisher;
    private final NotificationService notificationService;

    private final String uploadDir;

    public GrievanceCollaborationService(
            GrievanceRepository grievanceRepository,
            GrievanceCommentRepository commentRepository,
            GrievanceChatMessageRepository chatRepository,
            AttachmentRepository attachmentRepository,
            GrievanceRealtimePublisher realtimePublisher,
            NotificationService notificationService,
            @Value("${app.upload.dir:uploads}") String uploadDir
    ) {
        this.grievanceRepository = grievanceRepository;
        this.commentRepository = commentRepository;
        this.chatRepository = chatRepository;
        this.attachmentRepository = attachmentRepository;
        this.realtimePublisher = realtimePublisher;
        this.notificationService = notificationService;
        this.uploadDir = uploadDir;
    }

    private static boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority a : auth.getAuthorities()) {
            String v = a.getAuthority();
            for (String r : roles) {
                if (v.equalsIgnoreCase("ROLE_" + r)) return true;
            }
        }
        return false;
    }

    private void notifyStudentIfStaffUpdated(Grievance grievance, String updateSummary) {
        if (grievance == null) return;
        String studentEmail = grievance.getStudentEmail();
        if (studentEmail == null || studentEmail.isBlank()) return;

        boolean isStaff = hasAnyRole("FACULTY", "HOD", "PRINCIPAL", "COMMITTEE", "ADMIN", "SUPER_ADMIN");
        if (!isStaff) return;

        String code = grievance.getComplaintCode() != null && !grievance.getComplaintCode().isBlank()
                ? grievance.getComplaintCode()
                : String.valueOf(grievance.getId());

        String msg = updateSummary == null || updateSummary.isBlank()
                ? ("Complaint updated: " + code)
                : ("Complaint updated: " + code + " • " + updateSummary);

        notificationService.sendToUser(studentEmail, "COMPLAINT_UPDATED", msg, grievance.getId());
    }

    public GrievanceComment addComment(Long grievanceId, String authorEmail, String message, boolean internalOnly) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new IllegalArgumentException("Grievance not found"));

        GrievanceComment comment = new GrievanceComment();
        comment.setGrievance(grievance);
        comment.setAuthorEmail(authorEmail);
        comment.setMessage(message);
        comment.setInternalOnly(internalOnly);
        GrievanceComment saved = commentRepository.save(comment);
        realtimePublisher.publishGrievanceEvent(grievanceId, "COMMENT_ADDED");

        // Internal-only comments are staff notes; do not notify the student.
        if (!internalOnly) {
            notifyStudentIfStaffUpdated(grievance, "New comment added");
        }
        return saved;
    }

    public List<GrievanceComment> listComments(Long grievanceId) {
        return commentRepository.findByGrievanceIdOrderByCreatedAtAsc(grievanceId);
    }

    public GrievanceChatMessage addChatMessage(Long grievanceId, String senderEmail, String message) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new IllegalArgumentException("Grievance not found"));

        GrievanceChatMessage chat = new GrievanceChatMessage();
        chat.setGrievance(grievance);
        chat.setSenderEmail(senderEmail);
        chat.setMessage(message);
        GrievanceChatMessage saved = chatRepository.save(chat);
        realtimePublisher.publishGrievanceChatEvent(grievanceId, saved.getId());

        notifyStudentIfStaffUpdated(grievance, "New message received");
        return saved;
    }

    public List<GrievanceChatMessage> listChatMessages(Long grievanceId) {
        return chatRepository.findByGrievanceIdOrderByCreatedAtAsc(grievanceId);
    }

    public Attachment uploadAttachment(Long grievanceId, String uploadedByEmail, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new IllegalArgumentException("Grievance not found"));

        String safeOriginalName = Path.of(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                .getFileName()
                .toString();

        String storedFileName = UUID.randomUUID() + "__" + safeOriginalName;
        Path base = Path.of(uploadDir, "grievances", String.valueOf(grievanceId));
        try {
            Files.createDirectories(base);
            Path target = base.resolve(storedFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Attachment a = new Attachment();
            a.setGrievance(grievance);
            a.setOriginalFileName(safeOriginalName);
            a.setStoredFileName(storedFileName);
            a.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            a.setSizeBytes(file.getSize());
            a.setStoragePath(target.toAbsolutePath().toString());
            a.setUploadedByEmail(uploadedByEmail);
            Attachment saved = attachmentRepository.save(a);
            realtimePublisher.publishGrievanceEvent(grievanceId, "ATTACHMENT_ADDED");

            notifyStudentIfStaffUpdated(grievance, "New attachment uploaded");
            return saved;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public List<Attachment> listAttachments(Long grievanceId) {
        return attachmentRepository.findByGrievanceIdOrderByUploadedAtDesc(grievanceId);
    }

    public Resource loadAttachmentResource(Long attachmentId) {
        Attachment a = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        return new FileSystemResource(a.getStoragePath());
    }

    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
    }
}
