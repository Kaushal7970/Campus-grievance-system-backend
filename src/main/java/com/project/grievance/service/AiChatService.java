package com.project.grievance.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.grievance.dto.AiBotResponse;
import com.project.grievance.dto.AiChatResponse;

@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private final String provider;

    private final String openAiBaseUrl;
    private final String openAiApiKey;
    private final String openAiModel;

    private final String geminiBaseUrl;
    private final String geminiApiKey;
    private final String geminiModel;
    private final String geminiApiVersion;

    public AiChatService(
            ObjectMapper objectMapper,
            @Value("${app.ai.provider:openai}") String provider,
            @Value("${app.ai.openai.base-url:https://api.openai.com}") String openAiBaseUrl,
            @Value("${app.ai.openai.api-key:}") String openAiApiKey,
            @Value("${app.ai.openai.model:gpt-4o-mini}") String openAiModel,
            @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com}") String geminiBaseUrl,
            @Value("${app.ai.gemini.api-key:}") String geminiApiKey,
                @Value("${app.ai.gemini.model:gemini-1.5-flash}") String geminiModel,
                @Value("${app.ai.gemini.api-version:v1}") String geminiApiVersion
    ) {
        this.objectMapper = objectMapper;
        this.provider = provider == null ? "openai" : provider.trim();

        this.openAiBaseUrl = openAiBaseUrl;
        this.openAiApiKey = openAiApiKey;
        this.openAiModel = openAiModel;

        this.geminiBaseUrl = geminiBaseUrl;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
        this.geminiApiVersion = geminiApiVersion == null ? "v1" : geminiApiVersion.trim();

        this.restClient = RestClient.builder().build();
    }

    public AiChatResponse chat(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            AiChatResponse r = new AiChatResponse();
            r.setResponse("Please enter a message.");
            r.setCategory("GENERAL");
            r.setPriority("LOW");
            r.setSuggested_solution("Provide more details so we can help.");
            return r;
        }

        if ("openai".equalsIgnoreCase(provider)) {
            if (openAiApiKey == null || openAiApiKey.isBlank()) {
                return heuristic(userMessage);
            }

            try {
                Map<String, Object> payload = Map.of(
                        "model", openAiModel,
                        "temperature", 0.2,
                        "messages", new Object[] {
                                Map.of(
                                        "role", "system",
                                        "content",
                                        "You are a university helpdesk assistant. Return ONLY valid JSON with keys: response, category, priority, suggested_solution. " +
                                                "category must be one of: ACADEMIC, HOSTEL, INFRASTRUCTURE, FACULTY_BEHAVIOR, ADMINISTRATION. " +
                                                "priority must be one of: LOW, MEDIUM, HIGH, CRITICAL."
                                ),
                                Map.of(
                                        "role", "user",
                                        "content", userMessage
                                )
                        }
                );

                String raw = restClient.post()
                        .uri(openAiBaseUrl + "/v1/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(raw);
                String content = root.at("/choices/0/message/content").asText("");
                JsonNode parsed = objectMapper.readTree(content);

                AiChatResponse r = new AiChatResponse();
                r.setResponse(parsed.path("response").asText(""));
                r.setCategory(parsed.path("category").asText("GENERAL"));
                r.setPriority(parsed.path("priority").asText("LOW"));
                r.setSuggested_solution(parsed.path("suggested_solution").asText(""));
                return r;
            } catch (Exception ex) {
                return heuristic(userMessage);
            }
        }

        if ("gemini".equalsIgnoreCase(provider)) {
            if (geminiApiKey == null || geminiApiKey.isBlank()) {
                return heuristic(userMessage);
            }

            try {
                String prompt = "You are a university helpdesk assistant. Return ONLY valid JSON with keys: response, category, priority, suggested_solution. " +
                        "category must be one of: ACADEMIC, HOSTEL, INFRASTRUCTURE, FACULTY_BEHAVIOR, ADMINISTRATION. " +
                        "priority must be one of: LOW, MEDIUM, HIGH, CRITICAL." +
                        "\n\nUser: " + userMessage;

                String content = geminiGenerateText(prompt, 0.2);
                JsonNode parsed = objectMapper.readTree(content);

                AiChatResponse r = new AiChatResponse();
                r.setResponse(parsed.path("response").asText(""));
                r.setCategory(parsed.path("category").asText("GENERAL"));
                r.setPriority(parsed.path("priority").asText("LOW"));
                r.setSuggested_solution(parsed.path("suggested_solution").asText(""));
                return r;
            } catch (Exception ex) {
                return heuristic(userMessage);
            }
        }

        return heuristic(userMessage);
    }

    /**
     * General-purpose chatbot (no classification JSON required).
     */
    public AiBotResponse botChat(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return new AiBotResponse("Please enter a message.");
        }

        if ("openai".equalsIgnoreCase(provider)) {
            if (openAiApiKey == null || openAiApiKey.isBlank()) {
                return new AiBotResponse("AI is not configured on the server (missing OPENAI_API_KEY).");
            }

            try {
                Map<String, Object> payload = Map.of(
                        "model", openAiModel,
                        "temperature", 0.6,
                        "messages", new Object[] {
                                Map.of(
                                        "role", "system",
                                        "content", "You are a helpful campus assistant chatbot. Be concise, clear, and safe."
                                ),
                                Map.of(
                                        "role", "user",
                                        "content", userMessage
                                )
                        }
                );

                String raw = restClient.post()
                        .uri(openAiBaseUrl + "/v1/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(raw);
                String content = root.at("/choices/0/message/content").asText("");
                if (content.isBlank()) {
                    return new AiBotResponse("I couldn't generate a response right now. Please try again.");
                }
                return new AiBotResponse(content);
            } catch (Exception ex) {
                return new AiBotResponse("AI request failed. Please try again.");
            }
        }

        if ("gemini".equalsIgnoreCase(provider)) {
            if (geminiApiKey == null || geminiApiKey.isBlank()) {
                return new AiBotResponse("AI is not configured on the server (missing GEMINI_API_KEY).");
            }

            try {
                String prompt = "You are a helpful campus assistant chatbot. Be concise, clear, and safe." +
                        "\n\nUser: " + userMessage;
                String content = geminiGenerateText(prompt, 0.6);
                if (content.isBlank()) {
                    return new AiBotResponse("I couldn't generate a response right now. Please try again.");
                }
                return new AiBotResponse(content);
            } catch (Exception ex) {
                return new AiBotResponse(userFacingAiError(ex, "Gemini"));
            }
        }

        return new AiBotResponse("AI is not configured on the server.");
    }

    public Map<String, Object> status() {
        if ("openai".equalsIgnoreCase(provider)) {
            boolean enabled = openAiApiKey != null && !openAiApiKey.isBlank();
            return Map.of(
                    "provider", provider,
                    "enabled", enabled,
                    "model", openAiModel,
                    "baseUrl", openAiBaseUrl
            );
        }

        if ("gemini".equalsIgnoreCase(provider)) {
            boolean enabled = geminiApiKey != null && !geminiApiKey.isBlank();
            return Map.of(
                    "provider", provider,
                    "enabled", enabled,
                    "model", geminiModel,
                    "baseUrl", geminiBaseUrl
            );
        }

        return Map.of(
                "provider", provider,
                "enabled", false,
                "model", "",
                "baseUrl", ""
        );
    }

    private String geminiGenerateText(String prompt, double temperature) throws Exception {
        List<String> modelsToTry = new ArrayList<>();
        if (geminiModel != null && !geminiModel.isBlank()) {
            modelsToTry.add(geminiModel.trim());
        }

        // If the configured model is invalid for this API/key, fall back to a discovered supported model.
        // This keeps local dev working without requiring users to know the exact model name.
        List<String> discovered = null;

        RestClientResponseException lastHttpException = null;
        for (int i = 0; i < modelsToTry.size(); i++) {
            String model = modelsToTry.get(i);
            try {
                return geminiGenerateTextWithModel(prompt, temperature, model);
            } catch (RestClientResponseException ex) {
                lastHttpException = ex;
                // If model not found/unsupported, discover a working model once and retry.
                if (ex.getRawStatusCode() == 404 && discovered == null) {
                    discovered = geminiDiscoverGenerateContentModels();
                    for (String m : discovered) {
                        if (m != null && !m.isBlank() && !modelsToTry.contains(m)) {
                            modelsToTry.add(m);
                        }
                    }
                } else {
                    throw ex;
                }
            }
        }

        if (lastHttpException != null) {
            throw lastHttpException;
        }
        return "";
    }

    private String geminiGenerateTextWithModel(String prompt, double temperature, String model) throws Exception {
        String uri = geminiBaseUrl + "/" + geminiApiVersion + "/models/" + model + ":generateContent?key="
                + URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8);

        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", temperature
                )
        );

        String raw;
        try {
            raw = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            log.warn("Gemini API error status={} body={}", ex.getStatusCode(), safeTruncate(ex.getResponseBodyAsString(), 2000));
            throw ex;
        } catch (Exception ex) {
            log.warn("Gemini request failed", ex);
            throw ex;
        }

        JsonNode root = objectMapper.readTree(raw);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return "";
        }

        JsonNode parts = candidates.path(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return "";
        }

        List<String> texts = new ArrayList<>();
        for (JsonNode p : parts) {
            String t = p.path("text").asText("");
            if (!t.isBlank()) {
                texts.add(t);
            }
        }
        return String.join("\n", texts).trim();
    }

    private List<String> geminiDiscoverGenerateContentModels() {
        try {
            String uri = geminiBaseUrl + "/" + geminiApiVersion + "/models?key="
                    + URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8);

            String raw = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            JsonNode models = root.path("models");
            if (!models.isArray()) {
                return List.of();
            }

            List<String> names = new ArrayList<>();
            for (JsonNode m : models) {
                JsonNode methods = m.path("supportedGenerationMethods");
                boolean supports = false;
                if (methods.isArray()) {
                    for (JsonNode method : methods) {
                        if ("generateContent".equalsIgnoreCase(method.asText(""))) {
                            supports = true;
                            break;
                        }
                    }
                }
                if (!supports) continue;

                String name = m.path("name").asText("");
                if (name.startsWith("models/")) {
                    name = name.substring("models/".length());
                }
                if (!name.isBlank()) {
                    names.add(name);
                }
                if (names.size() >= 10) {
                    break;
                }
            }
            return names;
        } catch (Exception ex) {
            log.warn("Gemini model discovery failed", ex);
            return List.of();
        }
    }

    private String safeTruncate(String s, int maxLen) {
        if (s == null) return "";
        String cleaned = s.replaceAll("[\r\n\t]+", " ").trim();
        if (cleaned.length() <= maxLen) return cleaned;
        return cleaned.substring(0, maxLen) + "…";
    }

    private String userFacingAiError(Exception ex, String providerName) {
        if (ex instanceof RestClientResponseException r) {
            int code = r.getRawStatusCode();
            String body = r.getResponseBodyAsString();
            String apiMsg = "";
            try {
                JsonNode n = objectMapper.readTree(body);
                apiMsg = n.path("error").path("message").asText("");
            } catch (Exception ignore) {
                apiMsg = "";
            }

            String hint;
            if (code == 401 || code == 403) {
                hint = "Check API key and that the Generative Language API is enabled for your project.";
            } else if (code == 404 && "Gemini".equalsIgnoreCase(providerName)) {
                List<String> models = geminiDiscoverGenerateContentModels();
                if (!models.isEmpty()) {
                    hint = "Model not available. Set GEMINI_MODEL to one of: " + String.join(", ", models) + ".";
                } else {
                    hint = "Model not available. Try setting GEMINI_MODEL to a valid model for generateContent.";
                }
            } else if (code == 429) {
                hint = "Rate limit/quota reached. Try again later or increase quota.";
            } else if (code >= 500) {
                hint = "Provider temporary issue. Try again shortly.";
            } else {
                hint = "Request rejected. Verify configuration.";
            }

            String msg = apiMsg == null || apiMsg.isBlank() ? ("HTTP " + code) : ("HTTP " + code + ": " + apiMsg);
            return providerName + " error (" + msg + "). " + hint;
        }
        return providerName + " request failed. Please try again.";
    }

    private AiChatResponse heuristic(String msg) {
        String m = msg.toLowerCase(Locale.ROOT);

        String category = "ADMINISTRATION";
        if (containsAny(m, "hostel", "mess", "room", "warden")) category = "HOSTEL";
        else if (containsAny(m, "class", "lecture", "attendance", "exam", "marks", "result", "course")) category = "ACADEMIC";
        else if (containsAny(m, "water", "wifi", "network", "electric", "electricity", "fan", "light", "toilet", "road")) category = "INFRASTRUCTURE";
        else if (containsAny(m, "teacher", "faculty", "rude", "behavior", "harass", "abuse")) category = "FACULTY_BEHAVIOR";

        String priority = "LOW";
        if (containsAny(m, "urgent", "immediately", "today")) priority = "HIGH";
        if (containsAny(m, "danger", "fire", "threat", "assault")) priority = "CRITICAL";

        AiChatResponse r = new AiChatResponse();
        r.setCategory(category);
        r.setPriority(priority);
        r.setResponse("I can help. Based on your message, this looks like a " + category + " issue with " + priority + " priority.");
        r.setSuggested_solution(suggestSolution(category));
        return r;
    }

    private boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) return true;
        }
        return false;
    }

    private String suggestSolution(String category) {
        return switch (category) {
            case "HOSTEL" -> "Share hostel block/room and issue details; we will route to hostel administration.";
            case "ACADEMIC" -> "Provide course/subject, faculty name, and date/time; we will route to the academic cell.";
            case "INFRASTRUCTURE" -> "Provide location and photos if possible; we will route to maintenance.";
            case "FACULTY_BEHAVIOR" -> "Provide incident date/time and details; this will be routed confidentially.";
            default -> "Provide department and relevant details; we will route to the right team.";
        };
    }
}
