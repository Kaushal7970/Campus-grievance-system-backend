package com.project.grievance.service;

import java.security.SecureRandom;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.grievance.model.PasswordResetToken;
import com.project.grievance.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final PasswordResetTokenRepository tokenRepository;
    private final SecureRandom random = new SecureRandom();

    private final int otpLength;
    private final long otpTtlSeconds;
    private final boolean devReturnOtp;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            @Value("${app.otp.length:6}") int otpLength,
            @Value("${app.otp.ttl-seconds:600}") long otpTtlSeconds,
            @Value("${app.otp.dev-return:false}") boolean devReturnOtp
    ) {
        this.tokenRepository = tokenRepository;
        this.otpLength = otpLength;
        this.otpTtlSeconds = otpTtlSeconds;
        this.devReturnOtp = devReturnOtp;
    }

    public String createOtp(String email) {
        String otp = generateOtp(otpLength);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setOtp(otp);
        token.setExpiresAt(Instant.now().plusSeconds(otpTtlSeconds));
        tokenRepository.save(token);

        // Production should send via email/SMS. We log for development only.
        if (devReturnOtp) {
            log.warn("DEV OTP for {} is {} (set app.otp.dev-return=false in production)", email, otp);
            return otp;
        }

        log.info("Password reset OTP generated for {} (delivery not configured)", email);
        return null;
    }

    public boolean validateAndUseOtp(String email, String otp) {
        return tokenRepository.findTopByEmailAndOtpAndUsedFalseOrderByIdDesc(email, otp)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .map(t -> {
                    t.setUsed(true);
                    tokenRepository.save(t);
                    return true;
                })
                .orElse(false);
    }

    private String generateOtp(int length) {
        int bound = (int) Math.pow(10, length);
        int value = random.nextInt(bound);
        return String.format("%0" + length + "d", value);
    }
}
