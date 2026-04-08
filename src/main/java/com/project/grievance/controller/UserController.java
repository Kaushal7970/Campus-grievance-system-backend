package com.project.grievance.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.grievance.dto.ChangePasswordRequest;
import com.project.grievance.dto.FacultyOptionDto;
import com.project.grievance.dto.UpdateMyProfileRequest;
import com.project.grievance.dto.UpdateUserRequest;
import com.project.grievance.dto.UserProfileResponse;
import com.project.grievance.enums.Department;
import com.project.grievance.model.User;
import com.project.grievance.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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

    private static UserProfileResponse toProfile(User u) {
        UserProfileResponse p = new UserProfileResponse();
        p.setId(u.getId());
        p.setName(u.getName());
        p.setEmail(u.getEmail());
        p.setRole(u.getRole());
        p.setDepartment(u.getDepartment() == null ? null : String.valueOf(u.getDepartment()));
        p.setPhoneNumber(u.getPhoneNumber());
        p.setAvatarUrl(u.getAvatarUrl());
        p.setSmsNotificationsEnabled(u.isSmsNotificationsEnabled());
        p.setEmailNotificationsEnabled(u.isEmailNotificationsEnabled());
        p.setThemeId(u.getThemeId());
        p.setLastLoginAt(u.getLastLoginAt());
        return p;
    }

    private User mustGetMe() {
        String email = currentEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Not authenticated");
        }
        User me = service.findByEmail(email.trim().toLowerCase(Locale.ROOT));
        if (me == null) {
            throw new IllegalArgumentException("User not found");
        }
        return me;
    }

    // ✅ SELF PROFILE
    @GetMapping("/me")
    public UserProfileResponse me() {
        return toProfile(mustGetMe());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(@RequestBody UpdateMyProfileRequest request) {
        User me = mustGetMe();
        if (request == null) {
            return toProfile(me);
        }

        if (request.getName() != null) {
            me.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            me.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getSmsNotificationsEnabled() != null) {
            me.setSmsNotificationsEnabled(Boolean.TRUE.equals(request.getSmsNotificationsEnabled()));
        }
        if (request.getEmailNotificationsEnabled() != null) {
            me.setEmailNotificationsEnabled(Boolean.TRUE.equals(request.getEmailNotificationsEnabled()));
        }
        if (request.getThemeId() != null) {
            me.setThemeId(request.getThemeId());
        }

        return toProfile(service.save(me));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changeMyPassword(@RequestBody ChangePasswordRequest request) {
        User me = mustGetMe();

        String currentPassword = request == null ? null : request.getCurrentPassword();
        String newPassword = request == null ? null : request.getNewPassword();

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        if (!passwordEncoder.matches(currentPassword, me.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        me.setPassword(passwordEncoder.encode(newPassword));
        me.setTokenVersion(me.getTokenVersion() + 1);
        service.save(me);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/logout-all")
    public ResponseEntity<?> logoutAllSessions() {
        User me = mustGetMe();
        me.setTokenVersion(me.getTokenVersion() + 1);
        service.save(me);
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserProfileResponse uploadAvatar(@RequestParam("file") MultipartFile file) {
        User me = mustGetMe();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        if (file.getSize() > 2L * 1024L * 1024L) {
            throw new IllegalArgumentException("Avatar must be <= 2MB");
        }

        try {
            Path base = Path.of(uploadDir, "avatars", String.valueOf(me.getId()));
            Files.createDirectories(base);

            String original = file.getOriginalFilename();
            String safeName = (original == null ? "avatar" : original).replaceAll("[^a-zA-Z0-9._-]", "_");
            String targetName = Instant.now().toEpochMilli() + "_" + safeName;
            Path target = base.resolve(targetName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            me.setAvatarStoragePath(target.toAbsolutePath().toString());
            me.setAvatarUrl("/api/users/me/avatar");
            return toProfile(service.save(me));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to upload avatar");
        }
    }

    @GetMapping("/me/avatar")
    public ResponseEntity<Resource> getMyAvatar() {
        User me = mustGetMe();
        String path = me.getAvatarStoragePath();
        if (path == null || path.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // ✅ GET ALL USERS
    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    }

    // ✅ GET FACULTY OPTIONS (for assignment dropdowns)
    @GetMapping("/faculty")
    public List<FacultyOptionDto> getFacultyOptions() {
        return service.getAll().stream()
                .filter(u -> {
                    String r = u == null ? null : u.getRole();
                    return "FACULTY".equalsIgnoreCase(r) || "WARDEN".equalsIgnoreCase(r);
                })
                .map(u -> new FacultyOptionDto(u.getId(), u.getEmail(), u.getName()))
                .toList();
    }

    // ✅ GET USER BY ID
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // ✅ UPDATE ROLE (ADMIN CONTROL)
    @PutMapping("/{id}/role")
    public User updateRole(@PathVariable Long id, @RequestParam String role) {

        User user = service.getById(id);

        user.setRole(role.toUpperCase()); // 🔥 safe
        return service.save(user);
    }

    // ✅ UPDATE DEPARTMENT (ADMIN CONTROL)
    @PutMapping("/{id}/department")
    public User updateDepartment(@PathVariable Long id, @RequestParam String department) {
        User user = service.getById(id);
        Department d = department == null ? null : Department.valueOf(department.trim().toUpperCase());
        user.setDepartment(d);
        return service.save(user);
    }

    // ✅ UPDATE PROFILE (EMAIL)
    @PutMapping("/{id}/update")
    public User updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest updated) {

        User user = service.getById(id);

        // Only self or admin can update a profile.
        String email = currentEmail();
        boolean isPrivileged = hasAnyRole("ADMIN", "SUPER_ADMIN");
        if (!isPrivileged && (email == null || user.getEmail() == null || !email.equalsIgnoreCase(user.getEmail()))) {
            throw new IllegalArgumentException("Access denied");
        }

        if (updated != null) {
            // 🔥 only update allowed fields
            if (updated.getName() != null) {
                user.setName(updated.getName());
            }

            // Changing email impacts JWT subject; keep it admin-only.
            if (isPrivileged && updated.getEmail() != null && !updated.getEmail().isEmpty()) {
                user.setEmail(updated.getEmail());
            }

            if (updated.getPhoneNumber() != null) {
                user.setPhoneNumber(updated.getPhoneNumber());
            }

            if (updated.getThemeId() != null) {
                user.setThemeId(updated.getThemeId());
            }

            // Important: only overwrite prefs if caller provided them.
            if (updated.getSmsNotificationsEnabled() != null) {
                user.setSmsNotificationsEnabled(Boolean.TRUE.equals(updated.getSmsNotificationsEnabled()));
            }
            if (updated.getEmailNotificationsEnabled() != null) {
                user.setEmailNotificationsEnabled(Boolean.TRUE.equals(updated.getEmailNotificationsEnabled()));
            }
        }

        return service.save(user);
    }

    // 🔐 CHANGE PASSWORD
    @PutMapping("/{id}/change-password")
    public User changePassword(@PathVariable Long id, @RequestParam String password) {

        User user = service.getById(id);

        // Only self or admin can change a password.
        String email = currentEmail();
        boolean isPrivileged = hasAnyRole("ADMIN", "SUPER_ADMIN");
        if (!isPrivileged && (email == null || user.getEmail() == null || !email.equalsIgnoreCase(user.getEmail()))) {
            throw new IllegalArgumentException("Access denied");
        }

        if (password == null || password.isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setTokenVersion(user.getTokenVersion() + 1);
        return service.save(user);
    }

    // ❌ DELETE USER (ADMIN PROTECTED)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        User user = service.getById(id);

        // 🔥 Privileged accounts delete block
        if ("ADMIN".equalsIgnoreCase(user.getRole()) || "SUPER_ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Privileged user cannot be deleted");
        }

        service.delete(id);
    }
}