package com.ecoquest.config;

import com.ecoquest.ai.AIVerificationService;
import com.ecoquest.ai.GeminiAIVerificationService;
import com.ecoquest.ai.MockAIVerificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI_PROVIDER=mock (default) or gemini selects the implementation - see .env.example.
 * Only one AIVerificationService bean ever exists, so anything that autowires the interface
 * (QuestSubmissionService) doesn't need to know or care which provider is active.
 */
@Configuration
public class AIConfig {

    @Value("${app.ai.provider}")
    private String provider;

    @Value("${app.ai.gemini-api-key}")
    private String geminiApiKey;

    @Bean
    public MockAIVerificationService mockAIVerificationService() {
        return new MockAIVerificationService();
    }

    @Bean
    public AIVerificationService aiVerificationService(MockAIVerificationService mock) {
        if ("gemini".equalsIgnoreCase(provider)) {
            return new GeminiAIVerificationService(geminiApiKey, mock);
        }
        return mock;
    }
}
