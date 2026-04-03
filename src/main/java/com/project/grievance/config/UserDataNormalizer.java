package com.project.grievance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.project.grievance.repository.UserRepository;

@Component
public class UserDataNormalizer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataNormalizer.class);

    private final UserRepository userRepository;

    public UserDataNormalizer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        int updated = 0;
        for (var user : userRepository.findAll()) {
            if (user.normalizeStoredFields()) {
                userRepository.save(user);
                updated++;
            }
        }

        if (updated > 0) {
            log.info("Normalized {} user record(s) for email/role consistency", updated);
        }
    }
}
