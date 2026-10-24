package com.inkloom.integration.ai.mock;

import com.inkloom.integration.ai.BrandReviewAiProvider;
import com.inkloom.integration.ai.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of {@link BrandReviewAiProvider} for offline development,
 * regression testing, and CI environments without live API credentials.
 */
@Component("mockReviewAiProvider")
public class MockReviewAiProvider implements BrandReviewAiProvider {

    private boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getProviderName() {
        return "Mock Brand Review AI Engine";
    }

    @Override
    public String getModelName() {
        return "mock-governance-model-v1";
    }

    @Override
    public AiReviewResult analyzeContent(AiReviewRequest request) {
        AiReviewResult result = new AiReviewResult();
        result.setModelUsed(getModelName());
        result.setExecutionTimeMs(45);
        result.setSummary("Mock AI evaluation: Brand tone and factual claim integrity checked.");
        result.setOverallComplianceScore(88);

        List<AiFindingDto> findings = new ArrayList<>();
        String content = request.getContent() != null ? request.getContent() : "";

        // Check for common marketing buzzwords to simulate AI finding
        if (content.toLowerCase().contains("best-in-class") || content.toLowerCase().contains("revolutionary")) {
            AiFindingDto finding = new AiFindingDto();
            finding.setCategory("BRAND_TONE");
            finding.setSeverity("WARNING");
            finding.setEvidenceType("SUBJECTIVE_SUGGESTION");
            finding.setTitle("Subjective Superlative Detected");
            finding.setContentExcerpt(content.toLowerCase().contains("best-in-class") ? "best-in-class" : "revolutionary");
            finding.setExplanation("Superlatives without third-party audit citations violate corporate objectivity standards.");
            finding.setStartIndex(content.toLowerCase().indexOf(finding.getContentExcerpt()));
            finding.setEndIndex(finding.getStartIndex() + finding.getContentExcerpt().length());
            finding.setConfidence(0.92);

            // Connect to first approved source if available
            if (!request.getApprovedSources().isEmpty()) {
                finding.getReferencedSourceIds().add(request.getApprovedSources().get(0).getSourceId());
            }

            AiSuggestedCorrectionDto corr = new AiSuggestedCorrectionDto();
            corr.setLight("enterprise-grade");
            corr.setBalanced("industry-standard, rigorously tested");
            corr.setStrong("independently certified enterprise architecture");
            corr.setRationale("Preserves high-performance marketing impact while eliminating unsubstantiated superlatives.");
            finding.setSuggestedCorrections(corr);

            findings.add(finding);
        }

        result.setFindings(findings);
        return result;
    }
}
