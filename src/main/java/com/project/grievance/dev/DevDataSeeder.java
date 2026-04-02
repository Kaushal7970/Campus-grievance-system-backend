package com.project.grievance.dev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.project.grievance.model.User;
import com.project.grievance.repository.UserRepository;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final boolean seedAdminEnabled;
    private final String seedAdminEmail;
    private final String seedAdminPassword;

    public DevDataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.dev.seed-admin.enabled:true}") boolean seedAdminEnabled,
            @Value("${app.dev.seed-admin.email:admin@campus.local}") String seedAdminEmail,
            @Value("${app.dev.seed-admin.password:Admin@123}") String seedAdminPassword
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

        userRepository.findByEmail(seedAdminEmail.toLowerCase()).ifPresentOrElse(
                existing -> {
                    // already present
                },
                () -> {
                    User admin = new User();
                    admin.setName("Dev Admin");
                    admin.setEmail(seedAdminEmail.toLowerCase());
                    admin.setRole("ADMIN");
                    admin.setPassword(passwordEncoder.encode(seedAdminPassword));
                    userRepository.save(admin);
                }
        );
    }
}
