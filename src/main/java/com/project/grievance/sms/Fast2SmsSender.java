package com.project.grievance.sms;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class Fast2SmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(Fast2SmsSender.class);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public Fast2SmsSender(RestTemplate restTemplate, String apiKey, String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "https://www.fast2sms.com" : baseUrl;
    }

    @Override
    public void send(String toE164, String message) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Fast2SMS api-key not configured");
        }

        // Fast2SMS typically expects Indian numbers without +91.
        String numbers = toE164;
        if (numbers != null && numbers.startsWith("+91")) {
            numbers = numbers.substring(3);
        }
        if (numbers != null && numbers.startsWith("+")) {
            numbers = numbers.substring(1);
        }

        URI uri = URI.create(baseUrl + "/dev/bulkV2");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("authorization", apiKey);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("route", "q");
        form.add("message", message);
        form.add("numbers", numbers);

        ResponseEntity<String> response = restTemplate.postForEntity(uri.toString(), new HttpEntity<>(form, headers), String.class);
        int status = response.getStatusCode().value();
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("Fast2SMS failed with status " + status);
        }

        log.debug("Fast2SMS sent to {} status={}", toE164, status);
    }
}
