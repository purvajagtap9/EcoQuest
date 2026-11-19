package com.ecoquest.ai;

import com.ecoquest.entity.Quest;
import com.ecoquest.entity.embedded.VerificationResult;
import com.ecoquest.entity.enums.AiRecommendation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Real image analysis via the Gemini API. Never receives the API key from the frontend - it's
 * injected server-side from GEMINI_API_KEY (see AIConfig). If the call fails for ANY reason,
 * this falls back to MockAIVerificationService rather than blocking the submission flow -
 * spec #16/#35 requires the rest of the system to keep working even when the AI API is
 * unavailable.
 */
public class GeminiAIVerificationService implements AIVerificationService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIVerificationService.class);
    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s";

    private final String apiKey;
    private final MockAIVerificationService fallback;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiAIVerificationService(String apiKey, MockAIVerificationService fallback) {
        this.apiKey = apiKey;
        this.fallback = fallback;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public VerificationResult verifyEvidence(byte[] imageBytes, String imageContentType, Quest quest, String submissionDescription) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("AI_PROVIDER=gemini but GEMINI_API_KEY is not set - falling back to mock verification");
            return fallback.verifyEvidence(imageBytes, imageContentType, quest, submissionDescription);
        }

        try {
            String prompt = buildPrompt(quest, submissionDescription);
            String requestBody = buildRequestBody(prompt, imageContentType, imageBytes);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(ENDPOINT_TEMPLATE, apiKey)))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Gemini API returned status {} - falling back to mock verification", response.statusCode());
                return fallback.verifyEvidence(imageBytes, imageContentType, quest, submissionDescription);
            }

            return parseGeminiResponse(response.body());

        } catch (Exception e) {
            log.warn("Gemini API call failed ({}) - falling back to mock verification", e.toString());
            return fallback.verifyEvidence(imageBytes, imageContentType, quest, submissionDescription);
        }
    }

    private String buildPrompt(Quest quest, String submissionDescription) {
        return "You are reviewing photo evidence submitted for an environmental sustainability quest on an "
                + "educational platform for Indian students. "
                + "Quest title: \"" + quest.getTitle() + "\". "
                + "Quest description: \"" + quest.getDescription() + "\". "
                + "Quest category: " + quest.getCategory() + ". "
                + "Student's own description of what they did: \"" + (submissionDescription == null ? "" : submissionDescription) + "\". "
                + "Look at the attached image and judge whether it plausibly shows evidence of this specific activity. "
                + "Respond with ONLY a JSON object (no markdown, no prose) in exactly this shape: "
                + "{\"detected\": boolean, \"confidence\": number between 0 and 1, \"reason\": short string explaining your judgment, "
                + "\"recommendation\": one of \"LIKELY_VALID\", \"LIKELY_INVALID\", \"UNCERTAIN\"}. "
                + "This is advisory input for a human teacher who makes the final decision - be honest about uncertainty.";
    }

    private String buildRequestBody(String prompt, String mimeType, byte[] imageBytes) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType != null ? mimeType : "image/jpeg",
                                        "data", base64Image
                                ))
                        })
                },
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        return objectMapper.writeValueAsString(body);
    }

    private VerificationResult parseGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();

        JsonNode result = objectMapper.readTree(text);

        boolean detected = result.path("detected").asBoolean(false);
        double confidence = result.path("confidence").asDouble(0.5);
        String reason = result.path("reason").asText("No reason provided by AI.");
        String recommendationRaw = result.path("recommendation").asText("UNCERTAIN");

        AiRecommendation recommendation;
        try {
            recommendation = AiRecommendation.valueOf(recommendationRaw);
        } catch (IllegalArgumentException e) {
            recommendation = AiRecommendation.UNCERTAIN;
        }

        return new VerificationResult(detected, confidence, reason, recommendation, "gemini", Instant.now());
    }
}
