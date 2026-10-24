package com.inkloom.integration.ai;

import com.inkloom.integration.ai.dto.AiReviewRequest;
import com.inkloom.integration.ai.dto.AiReviewResult;

/**
 * Pluggable provider interface for AI-powered brand governance evaluations.
 * Decouples external AI engines (such as Google Gemini) from review orchestration,
 * persistence, and HTTP controllers.
 */
public interface BrandReviewAiProvider {

    /**
     * Executes brand governance review of submitted content against approved brand sources and guidelines.
     *
     * @param request the content and scoped brand documentation
     * @return structured evaluation results including findings and Intent Lock suggestions
     */
    AiReviewResult analyzeContent(AiReviewRequest request);

    /**
     * Indicates whether this AI provider is currently enabled in configuration.
     */
    boolean isEnabled();

    /**
     * Returns the human-readable identifier of this provider (e.g. "Google Gemini Developer API").
     */
    String getProviderName();

    /**
     * Returns the active model identifier (e.g. "gemini-2.5-flash").
     */
    String getModelName();
}
