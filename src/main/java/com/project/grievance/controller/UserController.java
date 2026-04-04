package com.project.grievance.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.grievance.dto.FacultyOptionDto;
import com.project.grievance.model.User;
import com.project.grievance.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final PasswordEncoder passwordEncoder;

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

    // ✅ GET ALL USERS
    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    }

    // ✅ GET FACULTY OPTIONS (for assignment dropdowns)
    @GetMapping("/faculty")
    public List<FacultyOptionDto> getFacultyOptions() {
        return service.getAll().stream()
                .filter(u -> "FACULTY".equalsIgnoreCase(u.getRole()))
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

    // ✅ UPDATE PROFILE (EMAIL)
    @PutMapping("/{id}/update")
    public User updateUser(@PathVariable Long id, @RequestBody User updated) {

        User user = service.getById(id);

        // Only self or admin can update a profile.
        String email = currentEmail();
        boolean isPrivileged = hasAnyRole("ADMIN", "SUPER_ADMIN");
        if (!isPrivileged && (email == null || user.getEmail() == null || !email.equalsIgnoreCase(user.getEmail()))) {
            throw new IllegalArgumentException("Access denied");
        }

        // 🔥 only update allowed fields
        if (updated.getEmail() != null && !updated.getEmail().isEmpty()) {
            user.setEmail(updated.getEmail());
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