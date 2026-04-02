package com.project.grievance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.grievance.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByEmailAndOtpAndUsedFalseOrderByIdDesc(String email, String otp);
}
