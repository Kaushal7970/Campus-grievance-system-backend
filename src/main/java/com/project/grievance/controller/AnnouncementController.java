package com.project.grievance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.project.grievance.dto.AnnouncementCreateRequest;
import com.project.grievance.enums.Role;
import com.project.grievance.model.Announcement;
import com.project.grievance.repository.AnnouncementRepository;
import com.project.grievance.service.AnnouncementRealtimePublisher;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementRepository repo;
    private final AnnouncementRealtimePublisher publisher;

    public AnnouncementController(AnnouncementRepository repo, AnnouncementRealtimePublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

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

    private static Role currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        for (GrantedAuthority a : auth.getAuthorities()) {
            String v = a.getAuthority();
            if (v != null && v.toUpperCase().startsWith("ROLE_")) {
                String r = v.substring("ROLE_".length()).toUpperCase();
                try {
                    return Role.valueOf(r);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }

    @GetMapping
    public List<Announcement> list() {
        if (hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "PRINCIPAL", "COMMITTEE")) {
            return repo.findAllByOrderByCreatedAtDesc();
        }

        Role role = currentRole();
        return repo.findByAudienceRoleIsNullOrAudienceRoleOrderByCreatedAtDesc(role);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Announcement create(@Validated @RequestBody AnnouncementCreateRequest req) {
        if (!hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "PRINCIPAL", "COMMITTEE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        String createdBy = currentEmail();
        if (createdBy == null) {
            throw new IllegalArgumentException("Unauthorized");
        }

        Announcement a = new Announcement();
        a.setTitle(req.getTitle());
        a.setMessage(req.getMessage());
        a.setCreatedByEmail(createdBy);

        String audience = req.getAudienceRole();
        if (audience != null && !audience.isBlank()) {
            try {
                a.setAudienceRole(Role.valueOf(audience.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid audienceRole");
            }
        }

        a = repo.save(a);
        publisher.publishAnnouncementEvent("CREATED", a.getId());
        return a;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!hasAnyRole("ADMIN", "SUPER_ADMIN", "HOD", "PRINCIPAL", "COMMITTEE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        if (!repo.existsById(id)) {
            return;
        }

        repo.deleteById(id);
        publisher.publishAnnouncementEvent("DELETED", id);
    }
}
