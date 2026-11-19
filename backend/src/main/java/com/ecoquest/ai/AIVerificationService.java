package com.ecoquest.ai;

import com.ecoquest.entity.Quest;
import com.ecoquest.entity.embedded.VerificationResult;

/**
 * Implementations are ADVISORY ONLY - the result is shown to the teacher on the review screen
 * (Phase 11's SubmissionDetailResponse) but never itself awards rewards or changes a
 * submission's final APPROVED/REJECTED outcome. See MockAIVerificationService (default,
 * works with no API key) and GeminiAIVerificationService (real analysis, falls back to mock
 * on any failure) - selected by AI_PROVIDER in application.yml / .env.
 */
public interface AIVerificationService {

    VerificationResult verifyEvidence(byte[] imageBytes, String imageContentType, Quest quest, String submissionDescription);
}
