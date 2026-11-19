package com.ecoquest.ai;

import com.ecoquest.entity.Quest;
import com.ecoquest.entity.embedded.VerificationResult;
import com.ecoquest.entity.enums.AiRecommendation;

import java.time.Instant;
import java.util.Random;

/**
 * Simulated verification - lets the whole submit -> review -> approve flow work end-to-end
 * with zero external setup (spec #35 / #16). NOT real image analysis; the reason text says
 * so explicitly so nobody mistakes it for one.
 */
public class MockAIVerificationService implements AIVerificationService {

    private final Random random = new Random();

    @Override
    public VerificationResult verifyEvidence(byte[] imageBytes, String imageContentType, Quest quest, String submissionDescription) {
        double base = 0.55 + random.nextDouble() * 0.35; // 0.55 - 0.90
        boolean hasDescription = submissionDescription != null && submissionDescription.trim().length() > 10;
        double confidence = Math.min(0.97, hasDescription ? base + 0.08 : base);

        AiRecommendation recommendation = confidence >= 0.75
                ? AiRecommendation.LIKELY_VALID
                : (confidence >= 0.55 ? AiRecommendation.UNCERTAIN : AiRecommendation.LIKELY_INVALID);

        String categoryLabel = quest.getCategory() != null
                ? quest.getCategory().name().toLowerCase().replace('_', ' ')
                : "the assigned";

        String reason = String.format(
                "[Simulated AI review] The submitted image appears broadly consistent with a %s activity. "
                        + "This is a mock verification for demo purposes, not a real image analysis - a human reviewer should confirm.",
                categoryLabel);

        return new VerificationResult(true, round(confidence), reason, recommendation, "mock", Instant.now());
    }

    private double round(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
