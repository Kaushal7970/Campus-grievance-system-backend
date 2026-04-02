package com.project.grievance.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.grievance.dto.AttachmentView;
import com.project.grievance.dto.ChatMessageView;
import com.project.grievance.dto.CommentView;
import com.project.grievance.dto.CreateChatMessageRequest;
import com.project.grievance.dto.CreateCommentRequest;
import com.project.grievance.dto.GrievanceView;
import com.project.grievance.dto.EscalationHistoryView;
import com.project.grievance.dto.StatusHistoryView;
import com.project.grievance.enums.Priority;
import com.project.grievance.enums.EscalationLevel;
import com.project.grievance.model.Attachment;
import com.project.grievance.model.Grievance;
import com.project.grievance.model.GrievanceComment;
import com.project.grievance.model.GrievanceEscalationHistory;
import com.project.grievance.model.GrievanceStatusHistory;
import com.project.grievance.repository.GrievanceEscalationHistoryRepository;
import com.project.grievance.repository.GrievanceStatusHistoryRepository;
import com.project.grievance.service.AttachmentMapper;
import com.project.grievance.service.GrievanceChatMessageMapper;
import com.project.grievance.service.GrievanceCollaborationService;
import com.project.grievance.service.GrievanceCommentMapper;
import com.project.grievance.service.GrievanceEscalationHistoryMapper;
import com.project.grievance.service.GrievanceMapper;
import com.project.grievance.service.GrievanceService;
import com.project.grievance.service.GrievanceStatusHistoryMapper;
import com.project.grievance.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/grievance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GrievanceController {

    private final GrievanceService service;
    private final GrievanceCollaborationService collaborationService;
    private final GrievanceStatusHistoryRepository statusHistoryRepository;
    private final GrievanceEscalationHistoryRepository escalationHistoryRepository;
    private final AuditLogService auditLogService;

    private static String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
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

    // CREATE
    @PostMapping("/create")
    public GrievanceView create(@RequestBody Grievance g, HttpServletRequest http) {
        if (g.getPriority() == null) {
            g.setPriority(Priority.LOW);
        }

        if (g.getStatus() == null) {
            g.setStatus("PENDING");
        }

        // Always store the real student email in DB; anonymity is handled at response level.
        String email = currentEmail();
        if (email != null && !hasAnyRole("ADMIN", "SUPER_ADMIN")) {
            g.setStudentEmail(email);
        }

        Grievance saved = service.save(g);
        auditLogService.log(currentEmail(), "GRIEVANCE_CREATE", "GRIEVANCE", String.valueOf(saved.getId()), saved.getComplaintCode(), http);
        return GrievanceMapper.toView(saved);
    }

    // ALL
    @GetMapping("/all")
    public List<GrievanceView> getAll() {
        return service.getAll().stream().map(GrievanceMapper::toView).toList();
    }

    // STUDENT
    @GetMapping("/student/{email}")
    public List<GrievanceView> getByStudent(@PathVariable String email) {
        String current = currentEmail();
        if (!hasAnyRole("ADMIN", "SUPER_ADMIN") && current != null && !current.equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Access denied");
        }
        return service.getByStudent(email).stream().map(GrievanceMapper::toView).toList();
    }

    // FACULTY
    @GetMapping("/faculty/{email}")
    public List<GrievanceView> getByFaculty(@PathVariable String email) {
        String current = currentEmail();
        if (!hasAnyRole("ADMIN", "SUPER_ADMIN") && current != null && !current.equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Access denied");
        }

        return service.getByFaculty(email)
                .stream()
                .map(g -> {
                    GrievanceView v = GrievanceMapper.toView(g);
                    if (g.isAnonymous() && !hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "COMMITTEE")) {
                        v.setStudentEmail(null);
                    }
                    return v;
                })
                .toList();
    }

    // SINGLE
    @GetMapping("/{id}")
    public GrievanceView getOne(@PathVariable Long id) {
        Grievance g = service.getById(id);
        GrievanceView v = GrievanceMapper.toView(g);
        if (g.isAnonymous() && !hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "COMMITTEE")) {
            v.setStudentEmail(null);
        }
        return v;
    }

    // UPDATE STATUS
    @PutMapping("/update/{id}")
    public GrievanceView updateStatus(@PathVariable Long id, @RequestParam String status, HttpServletRequest http) {
        Grievance updated = service.updateStatus(id, status);
        auditLogService.log(currentEmail(), "GRIEVANCE_STATUS", "GRIEVANCE", String.valueOf(id), status, http);
        return GrievanceMapper.toView(updated);
    }

    // ASSIGN
    @PutMapping("/assign/{id}")
    public GrievanceView assign(@PathVariable Long id, @RequestParam String facultyEmail, HttpServletRequest http) {
        Grievance updated = service.assign(id, facultyEmail);
        auditLogService.log(currentEmail(), "GRIEVANCE_ASSIGN", "GRIEVANCE", String.valueOf(id), facultyEmail, http);
        return GrievanceMapper.toView(updated);
    }

    // UPDATE WITH REMARKS
    @PutMapping("/update-with-remarks/{id}")
    public GrievanceView updateWithRemarks(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remarks,
            HttpServletRequest http
    ) {
        Grievance updated = service.updateWithRemarks(id, status, remarks);
        auditLogService.log(currentEmail(), "GRIEVANCE_REMARKS", "GRIEVANCE", String.valueOf(id), status, http);
        return GrievanceMapper.toView(updated);
    }

    // STATUS HISTORY
    @GetMapping("/{id}/history")
    public List<StatusHistoryView> history(@PathVariable Long id) {
        List<GrievanceStatusHistory> history = statusHistoryRepository.findByGrievanceIdOrderByChangedAtAsc(id);
        return history.stream().map(GrievanceStatusHistoryMapper::toView).toList();
    }

    // ESCALATION HISTORY
    @GetMapping("/{id}/escalations")
    public List<EscalationHistoryView> escalations(@PathVariable Long id) {
        List<GrievanceEscalationHistory> history = escalationHistoryRepository.findByGrievanceIdOrderByTriggeredAtAsc(id);
        return history.stream().map(GrievanceEscalationHistoryMapper::toView).toList();
    }

    // MANUAL ESCALATION (privileged)
    @PostMapping("/{id}/escalate")
    public GrievanceView manualEscalate(
            @PathVariable Long id,
            @RequestParam("level") String level,
            @RequestParam(value = "reason", required = false) String reason,
            HttpServletRequest http
    ) {
        if (!hasAnyRole("HOD", "PRINCIPAL", "ADMIN", "SUPER_ADMIN")) {
            throw new IllegalArgumentException("Access denied");
        }

        EscalationLevel to = EscalationLevel.valueOf(String.valueOf(level).toUpperCase());
        Grievance updated = service.escalate(id, to, currentEmail(), false, reason);
        auditLogService.log(currentEmail(), "GRIEVANCE_ESCALATE", "GRIEVANCE", String.valueOf(id), to.name(), http);
        return GrievanceMapper.toView(updated);
    }

    // COMMENTS
    @PostMapping("/{id}/comments")
    public CommentView addComment(@PathVariable Long id, @RequestBody CreateCommentRequest request, HttpServletRequest http) {
        String email = currentEmail();
        boolean internalOnly = request.isInternalOnly()
                && hasAnyRole("FACULTY", "HOD", "COMMITTEE", "ADMIN", "SUPER_ADMIN");
        GrievanceComment saved = collaborationService.addComment(
                id,
                email == null ? "UNKNOWN" : email,
                request.getMessage(),
                internalOnly
        );
        auditLogService.log(email, "GRIEVANCE_COMMENT", "GRIEVANCE", String.valueOf(id), null, http);
        return GrievanceCommentMapper.toView(saved);
    }

    @GetMapping("/{id}/comments")
    public List<CommentView> listComments(@PathVariable Long id) {
        boolean privileged = hasAnyRole("FACULTY", "HOD", "COMMITTEE", "ADMIN", "SUPER_ADMIN");
        return collaborationService.listComments(id)
                .stream()
                .filter(c -> privileged || !c.isInternalOnly())
                .map(GrievanceCommentMapper::toView)
                .toList();
    }

    // LIVE CHAT (non-AI, human-to-human)
    @PostMapping("/{id}/chat")
    public ChatMessageView sendChat(@PathVariable Long id, @RequestBody CreateChatMessageRequest request, HttpServletRequest http) {
        String email = currentEmail();
        if (email == null) {
            throw new IllegalArgumentException("Unauthorized");
        }

        var saved = collaborationService.addChatMessage(id, email, request.getMessage());
        auditLogService.log(email, "GRIEVANCE_CHAT", "GRIEVANCE", String.valueOf(id), null, http);
        return GrievanceChatMessageMapper.toView(saved);
    }

    @GetMapping("/{id}/chat")
    public List<ChatMessageView> listChat(@PathVariable Long id) {
        return collaborationService.listChatMessages(id)
                .stream()
                .map(GrievanceChatMessageMapper::toView)
                .toList();
    }

    // ATTACHMENTS
    @PostMapping("/{id}/attachments")
    public AttachmentView uploadAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file, HttpServletRequest http) {
        String email = currentEmail();
        Attachment a = collaborationService.uploadAttachment(id, email == null ? "UNKNOWN" : email, file);
        auditLogService.log(email, "GRIEVANCE_ATTACHMENT", "GRIEVANCE", String.valueOf(id), a.getOriginalFileName(), http);
        return AttachmentMapper.toView(a);
    }

    @GetMapping("/{id}/attachments")
    public List<AttachmentView> listAttachments(@PathVariable Long id) {
        return collaborationService.listAttachments(id).stream().map(AttachmentMapper::toView).toList();
    }

    @GetMapping(value = "/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long attachmentId) {
        Attachment a = collaborationService.getAttachment(attachmentId);
        Resource resource = collaborationService.loadAttachmentResource(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + a.getOriginalFileName().replace("\"", "") + "\""
                )
                .body(resource);
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
