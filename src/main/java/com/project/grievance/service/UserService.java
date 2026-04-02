package com.project.grievance.service;

import com.project.grievance.model.User;
import com.project.grievance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

        // 🔥 ADMIN DELETE BLOCK (DOUBLE SAFETY)
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Admin cannot be deleted");
        }

        repo.deleteById(id);
    }
}