package com.project.grievance.sms;

import java.net.URI;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class TwilioSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsSender.class);

    private final RestTemplate restTemplate;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final String baseUrl;

    public TwilioSmsSender(RestTemplate restTemplate, String accountSid, String authToken, String fromNumber, String baseUrl) {
        this.restTemplate = restTemplate;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "https://api.twilio.com" : baseUrl;
    }

    @Override
    public void send(String toE164, String message) {
        if (accountSid == null || accountSid.isBlank() || authToken == null || authToken.isBlank()) {
            throw new IllegalStateException("Twilio credentials not configured");
        }
        if (fromNumber == null || fromNumber.isBlank()) {
            throw new IllegalStateException("Twilio from-number not configured");
        }

        URI uri = URI.create(String.format("%s/2010-04-01/Accounts/%s/Messages.json", baseUrl, accountSid));

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(
            Objects.requireNonNull(accountSid, "accountSid"),
            Objects.requireNonNull(authToken, "authToken")
        );
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", toE164);
        form.add("From", fromNumber);
        form.add("Body", message);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri.toString(), new HttpEntity<>(form, headers), String.class);
            int status = response.getStatusCode().value();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Twilio SMS failed with status " + status);
            }

            log.debug("Twilio SMS sent to {} status={}", toE164, status);
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            if (body == null) {
                body = "";
            }
            body = body.replace('\n', ' ').replace('\r', ' ').trim();
            if (body.length() > 500) {
                body = body.substring(0, 500) + "…";
            }
            throw new IllegalStateException(
                    "Twilio SMS failed status=" + e.getRawStatusCode() +
                            (body.isBlank() ? "" : (" body=" + body)),
                    e
            );
        }
    }
}
