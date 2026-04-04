package com.project.grievance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.project.grievance.model.User;
import com.project.grievance.repository.UserRepository;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final boolean seedAdminEnabled;
    private final String seedAdminEmail;
    private final String seedAdminPassword;

    public AdminSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed-admin.enabled:false}") boolean seedAdminEnabled,
            @Value("${app.seed-admin.email:}") String seedAdminEmail,
            @Value("${app.seed-admin.password:}") String seedAdminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedAdminEnabled = seedAdminEnabled;
        this.seedAdminEmail = seedAdminEmail;
        this.seedAdminPassword = seedAdminPassword;
    }

    @Override
    public void run(String... args) {
        if (!seedAdminEnabled) {
            return;
        }

        String email = seedAdminEmail == null ? "" : seedAdminEmail.trim().toLowerCase();
        if (email.isBlank() || seedAdminPassword == null || seedAdminPassword.isBlank()) {
            return;
        }

        userRepository.findByEmail(email).ifPresentOrElse(
                existing -> {
                    existing.setRole("ADMIN");
                    if (!passwordEncoder.matches(seedAdminPassword, existing.getPassword())) {
                        existing.setPassword(passwordEncoder.encode(seedAdminPassword));
                    }
                    userRepository.save(existing);
                },
                () -> {
                    User admin = new User();
                    admin.setName("System Admin");
                    admin.setEmail(email);
                    admin.setRole("ADMIN");
                    admin.setPassword(passwordEncoder.encode(seedAdminPassword));
                    userRepository.save(admin);
                }
        );
    }
}