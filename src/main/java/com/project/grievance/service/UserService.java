package com.project.grievance.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.grievance.model.User;
import com.project.grievance.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;

    // 🔍 FIND BY EMAIL
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }

    // 💾 SAVE USER
    public User save(User user) {
        return repo.save(user);
    }

    // 📄 GET ALL USERS
    public List<User> getAll() {
        return repo.findAll();
    }

    // 🔍 GET BY ID (🔥 NEW)
    public User getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ❌ DELETE USER (ADMIN PROTECTED)
    public void delete(Long id) {

        User user = getById(id);

        // 🔥 Privileged accounts delete block (DOUBLE SAFETY)
        if ("ADMIN".equalsIgnoreCase(user.getRole()) || "SUPER_ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Privileged user cannot be deleted");
        }

        repo.deleteById(id);
    }
}