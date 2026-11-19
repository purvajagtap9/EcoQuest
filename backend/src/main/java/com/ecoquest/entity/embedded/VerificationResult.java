package com.ecoquest.entity.embedded;

import com.ecoquest.entity.enums.AiRecommendation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Embedded inside QuestSubmission. Always accessed together with the submission on the
 * teacher review screen, so it doesn't need to be a separate collection.
 * IMPORTANT: this is ADVISORY ONLY — it never awards rewards by itself (see business rule #9).
 * A teacher (or admin override) is always the final authority; see QuestSubmission.status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResult {
    private Boolean detected;
    private Double confidence;          // 0.0 - 1.0
    private String reason;
    private AiRecommendation recommendation;
    private String provider;            // "mock" | "gemini"
    private Instant verifiedAt;
}
