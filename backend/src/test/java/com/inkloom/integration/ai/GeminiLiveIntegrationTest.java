package com.inkloom.integration.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkloom.integration.ai.config.GeminiProperties;
import com.inkloom.integration.ai.dto.AiApprovedClaimSnippet;
import com.inkloom.integration.ai.dto.AiApprovedSourceSnippet;
import com.inkloom.integration.ai.dto.AiReviewRequest;
import com.inkloom.integration.ai.dto.AiReviewResult;
import com.inkloom.integration.ai.gemini.GeminiReviewAiProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live integration test connecting to the real Google Gemini Developer API.
 * Automatically runs ONLY when a real GOOGLE_API_KEY environment variable is present.
 * Routine builds and CI runs safely skip this test without failure.
 */
@Tag("integration-live-ai")
class GeminiLiveIntegrationTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "GOOGLE_API_KEY", matches = ".+")
    @DisplayName("Live Gemini Call: Evaluates brand text against approved sources with structured JSON output")
    void liveGeminiReview_ShouldReturnStructuredFindings() {
        GeminiProperties properties = new GeminiProperties();
        properties.setEnabled(true);
        // Properties automatically picks up GOOGLE_API_KEY from environment
        assertNotNull(properties.getApiKey(), "GOOGLE_API_KEY must be populated from environment");
        assertFalse(properties.getApiKey().isEmpty(), "GOOGLE_API_KEY cannot be empty");

        ObjectMapper objectMapper = new ObjectMapper();
        GeminiReviewAiProvider provider = new GeminiReviewAiProvider(properties, objectMapper);

        AiReviewRequest request = new AiReviewRequest();
        request.setBrandName("Acme Enterprise");
        request.setContent("Acme's revolutionary cloud platform provides 100% unbreakable security and the fastest speeds in the galaxy.");

        // Provide verified ground-truth sources
        request.setApprovedSources(List.of(
                new AiApprovedSourceSnippet("src-soc2", "SOC2 Security Audit Report 2026", "AUDIT", "Standard enterprise encryption (AES-256) implemented. High security with 99.9% availability.")
        ));

        // Provide approved claims
        request.setApprovedClaims(List.of(
                new AiApprovedClaimSnippet("clm-uptime", "99.9% uptime SLA", "src-soc2", "Cloud Services", false)
        ));

        request.setProhibitedExpressions(List.of("revolutionary", "unbreakable", "fastest in the galaxy"));

        System.out.println("Executing Live Google Gemini Integration Test with model: " + properties.getModel());

        AiReviewResult result = provider.analyzeContent(request);

        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getFindings(), "Findings should not be null");
        assertFalse(result.getFindings().isEmpty(), "Gemini should detect governance violations in the hyperbolic prompt");

        System.out.println("Live Gemini Review Succeeded!");
        System.out.println("Model Used: " + result.getModelUsed());
        System.out.println("Execution Time: " + result.getExecutionTimeMs() + "ms");
        System.out.println("Summary: " + result.getSummary());
        System.out.println("Total Findings Identified: " + result.getFindings().size());

        result.getFindings().forEach(f -> {
            System.out.println(" - [" + f.getSeverity() + "] " + f.getTitle() + " (Excerpt: \"" + f.getContentExcerpt() + "\")");
            if (f.getSuggestedCorrections() != null) {
                System.out.println("    Intent Lock Balanced Fix: " + f.getSuggestedCorrections().getBalanced());
            }
        });

        assertTrue(result.getExecutionTimeMs() > 0);
        assertNotNull(result.getModelUsed());
    }
}
