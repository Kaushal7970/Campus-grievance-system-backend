package com.project.grievance.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SmsConfig {

    @Bean
    public RestTemplate smsRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SmsSender smsSender(
            RestTemplate smsRestTemplate,
            @Value("${app.sms.enabled:false}") boolean enabled,
            @Value("${app.sms.provider:noop}") String provider,
            @Value("${app.sms.twilio.account-sid:}") String twilioSid,
            @Value("${app.sms.twilio.auth-token:}") String twilioToken,
            @Value("${app.sms.twilio.from-number:}") String twilioFrom,
            @Value("${app.sms.twilio.base-url:https://api.twilio.com}") String twilioBaseUrl,
            @Value("${app.sms.fast2sms.api-key:}") String fast2smsKey,
            @Value("${app.sms.fast2sms.base-url:https://www.fast2sms.com}") String fast2smsBaseUrl
    ) {
        if (!enabled) {
            return new NoopSmsSender();
        }

        String p = provider == null ? "" : provider.trim().toLowerCase();
        return switch (p) {
            case "twilio" -> new TwilioSmsSender(smsRestTemplate, twilioSid, twilioToken, twilioFrom, twilioBaseUrl);
            case "fast2sms" -> new Fast2SmsSender(smsRestTemplate, fast2smsKey, fast2smsBaseUrl);
            default -> new NoopSmsSender();
        };
    }
}
