package com.project.grievance.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import com.project.grievance.dto.AuthRequest;
import com.project.grievance.dto.AuthResponse;
import com.project.grievance.dto.ForgotPasswordRequest;
import com.project.grievance.dto.RegisterRequest;
import com.project.grievance.dto.ResetPasswordRequest;
import com.project.grievance.model.User;
import com.project.grievance.repository.UserRepository;
import com.project.grievance.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    private final int lockoutMaxAttempts;
    private final Duration lockoutDuration;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PasswordResetService passwordResetService,
            @Value("${app.security.lockout.max-attempts:5}") int lockoutMaxAttempts,
            @Value("${app.security.lockout.minutes:15}") long lockoutMinutes
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
        this.lockoutMaxAttempts = lockoutMaxAttempts;
        this.lockoutDuration = Duration.ofMinutes(lockoutMinutes);
    }

    public User register(RegisterRequest request) {
        // Public self-registration: do not allow choosing elevated roles.
        String role = "STUDENT";
        String email = request.getEmail().toLowerCase();

        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered");
        });

        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        Instant now = Instant.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new IllegalArgumentException("Account locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= lockoutMaxAttempts) {
                user.setLockedUntil(now.plus(lockoutDuration));
                user.setFailedLoginAttempts(0);
            }
            userRepository.save(user);
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Successful login: reset lockout counters
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        userRepository.save(user);

        String token = jwtService.generateAccessToken(user.getEmail(), List.of(user.getRole().toUpperCase()));

        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setToken(token);
        response.setRole(user.getRole());
        response.setEmail(user.getEmail());
        return response;
    }

    /**
     * Creates an OTP for password reset.
     *
     * <p>To reduce user enumeration, unknown emails behave like success but do not create an OTP.</p>
     */
    public String forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return null;
        }

        return passwordResetService.createOtp(email);
    }

    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or OTP"));

        boolean ok = passwordResetService.validateAndUseOtp(email, request.getOtp());
        if (!ok) {
            throw new IllegalArgumentException("Invalid email or OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
