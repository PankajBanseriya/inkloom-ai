package com.inkloom.integration.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.inkloom.integration.ai.config.GeminiProperties;
import com.inkloom.integration.ai.dto.*;
import com.inkloom.integration.ai.exception.*;
import com.inkloom.integration.ai.gemini.GeminiReviewAiProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiReviewAiProviderTest {

    private GeminiProperties properties;
    private ObjectMapper objectMapper;
    private GeminiReviewAiProvider provider;

    @BeforeEach
    void setUp() {
        properties = new GeminiProperties();
        objectMapper = new ObjectMapper();
        provider = new GeminiReviewAiProvider(properties, objectMapper);
    }

    @Test
    @DisplayName("Should throw AiDisabledException when AI is disabled in configuration")
    void analyzeContent_WhenDisabled_ShouldThrowAiDisabledException() {
        properties.setEnabled(false);
        properties.setApiKey("test-key");

        AiReviewRequest request = new AiReviewRequest();
        request.setContent("Test content for review.");

        assertThrows(AiDisabledException.class, () -> provider.analyzeContent(request));
    }

    @Test
    @DisplayName("Should throw AiConfigurationException when enabled but API key is missing or blank")
    void analyzeContent_WhenEnabledWithoutKey_ShouldThrowAiConfigurationException() {
        properties.setEnabled(true);
        properties.setApiKey("");

        AiReviewRequest request = new AiReviewRequest();
        request.setContent("Test content for review.");

        AiConfigurationException ex = assertThrows(AiConfigurationException.class, () -> provider.analyzeContent(request));
        assertTrue(ex.getMessage().contains("GOOGLE_API_KEY"));
    }

    @Test
    @DisplayName("Should return 100 compliance score for empty content without calling provider")
    void analyzeContent_WhenContentEmpty_ShouldReturnEmptyResult() {
        properties.setEnabled(true);
        properties.setApiKey("valid-key");

        AiReviewRequest request = new AiReviewRequest();
        request.setContent("");

        AiReviewResult result = provider.analyzeContent(request);
        assertNotNull(result);
        assertEquals(100, result.getOverallComplianceScore());
        assertTrue(result.getFindings().isEmpty());
    }

    @Test
    @DisplayName("Should throw AiProviderException when content exceeds maximum allowed input size")
    void analyzeContent_WhenContentExceedsLimit_ShouldThrowException() {
        properties.setEnabled(true);
        properties.setApiKey("valid-key");
        properties.setMaxInputChars(50);

        AiReviewRequest request = new AiReviewRequest();
        request.setContent("This sentence is intentionally made long enough to exceed fifty characters limit.");

        assertThrows(AiProviderException.class, () -> provider.analyzeContent(request));
    }

    @Test
    @DisplayName("Zero-Hallucination Guard: Should filter out non-existent/hallucinated source IDs")
    void parseAndValidateResponse_ShouldFilterHallucinatedSourceIds() {
        AiReviewRequest request = new AiReviewRequest();
        request.setContent("Our revolutionary enterprise platform is the world's most scalable database.");
        request.setApprovedSources(List.of(
                new AiApprovedSourceSnippet("src-valid-01", "Database Architecture Benchmark", "BENCHMARK", "Scalability up to 10M IOPS.")
        ));

        // Simulated Gemini JSON output where model referenced both the valid source and an invented one
        String jsonPayload = """
        {
          "summary": "Detected unverified superlative and prohibited buzzword.",
          "overallComplianceScore": 75,
          "findings": [
            {
              "category": "BRAND_TONE",
              "severity": "WARNING",
              "evidenceType": "SUBJECTIVE_SUGGESTION",
              "title": "Unsubstantiated Superlative",
              "contentExcerpt": "world's most scalable",
              "explanation": "Claim lacks third-party audit verification.",
              "referencedSourceIds": ["src-valid-01", "src-hallucinated-999"],
              "suggestedCorrections": {
                "light": "highly scalable",
                "balanced": "independently verified high-throughput",
                "strong": "certified high-availability distributed architecture",
                "rationale": "Eliminates ungrounded superlatives while keeping technical emphasis."
              },
              "confidence": 0.95
            }
          ]
        }
        """;

        // Call the package-private parseAndValidateResponse method directly
        TestableGeminiProvider testableProvider = new TestableGeminiProvider(properties, objectMapper);
        AiReviewResult result = testableProvider.testParseAndValidate(jsonPayload, request);

        assertNotNull(result);
        assertEquals(75, result.getOverallComplianceScore());
        assertEquals(1, result.getFindings().size());

        AiFindingDto finding = result.getFindings().get(0);
        assertEquals("world's most scalable", finding.getContentExcerpt());
        assertEquals(45, finding.getStartIndex()); // Substring offset match in content
        assertEquals(66, finding.getEndIndex());

        // Verify Zero-Hallucination Filter: valid ID retained, hallucinated ID discarded!
        assertEquals(1, finding.getReferencedSourceIds().size());
        assertTrue(finding.getReferencedSourceIds().contains("src-valid-01"));
        assertFalse(finding.getReferencedSourceIds().contains("src-hallucinated-999"));

        // Verify Intent Lock suggestions
        assertNotNull(finding.getSuggestedCorrections());
        assertEquals("highly scalable", finding.getSuggestedCorrections().getLight());
        assertEquals("independently verified high-throughput", finding.getSuggestedCorrections().getBalanced());
        assertTrue(finding.getSuggestedCorrections().isPreservesIntent());
    }

    @Test
    @DisplayName("Should safely reject malformed or invalid JSON payloads from AI")
    void parseAndValidateResponse_WhenMalformedJson_ShouldThrowAiProviderException() {
        AiReviewRequest request = new AiReviewRequest();
        request.setContent("Valid content.");

        String malformedJson = "{ invalid-json: not closed ";

        TestableGeminiProvider testableProvider = new TestableGeminiProvider(properties, objectMapper);
        assertThrows(AiProviderException.class, () -> testableProvider.testParseAndValidate(malformedJson, request));
    }

    /**
     * Test subclass exposing parseAndValidateResponse for white-box unit testing.
     */
    private static class TestableGeminiProvider extends GeminiReviewAiProvider {
        public TestableGeminiProvider(GeminiProperties properties, ObjectMapper objectMapper) {
            super(properties, objectMapper);
        }

        public AiReviewResult testParseAndValidate(String json, AiReviewRequest request) {
            return super.parseAndValidateResponse(json, request);
        }
    }
}
