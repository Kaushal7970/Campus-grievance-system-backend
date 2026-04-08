package com.project.grievance.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final SmsSender sender;
    private final boolean enabled;
    private final int maxLength;
    private final int maxAttempts;

    public SmsService(
            SmsSender sender,
            @Value("${app.sms.enabled:false}") boolean enabled,
            @Value("${app.sms.max-length:160}") int maxLength,
            @Value("${app.sms.max-attempts:3}") int maxAttempts
    ) {
        this.sender = sender;
        this.enabled = enabled;
        this.maxLength = Math.max(40, maxLength);
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    public void sendSafe(String toE164, String message) {
        if (!enabled) {
            return;
        }

        String to = toE164 == null ? null : toE164.trim();
        if (to == null || to.isBlank()) {
            return;
        }

        String body = sanitize(message);
        if (body.isBlank()) {
            return;
        }

        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                sender.send(to, body);
                return;
            } catch (Exception e) {
                last = e;
                log.warn("SMS send failed attempt {}/{} to={} err={}", attempt, maxAttempts, to, e.toString());
                // best-effort backoff (keep small to avoid blocking main request)
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(200L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (last != null) {
            log.error("SMS send ultimately failed to={}", to, last);
        }
    }

    private String sanitize(String message) {
        if (message == null) {
            return "";
        }
        String v = message.replace('\n', ' ').replace('\r', ' ').trim();
        while (v.contains("  ")) {
            v = v.replace("  ", " ");
        }
        if (v.length() <= maxLength) {
            return v;
        }
        // Truncate, keep message readable
        String cut = v.substring(0, Math.max(0, maxLength - 1)).trim();
        return cut + "…";
    }
}
