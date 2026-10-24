package com.inkloom.integration.ai;

import com.inkloom.integration.ai.config.GeminiProperties;
import com.inkloom.integration.ai.dto.*;
import com.inkloom.integration.ai.exception.AiConfigurationException;
import com.inkloom.integration.ai.mock.MockReviewAiProvider;
import com.inkloom.integration.ai.orchestration.ReviewAiOrchestrator;
import com.inkloom.model.*;
import com.inkloom.repository.ApprovedClaimRepository;
import com.inkloom.repository.ApprovedSourceRepository;
import com.inkloom.repository.BrandGuidelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewAiOrchestratorTest {

    @Mock
    private BrandGuidelineRepository guidelineRepository;
    @Mock
    private ApprovedSourceRepository sourceRepository;
    @Mock
    private ApprovedClaimRepository claimRepository;

    private GeminiProperties geminiProperties;
    private MockReviewAiProvider mockAiProvider;
    private ReviewAiOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        geminiProperties = new GeminiProperties();
        mockAiProvider = new MockReviewAiProvider();

        orchestrator = new ReviewAiOrchestrator(
                mockAiProvider,
                geminiProperties,
                guidelineRepository,
                sourceRepository,
                claimRepository
        );
    }

    @Test
    @DisplayName("Should return empty list when AI review is disabled in properties")
    void performAiReview_WhenDisabled_ShouldReturnEmptyList() {
        geminiProperties.setEnabled(false);

        List<Finding> findings = orchestrator.performAiReview("brand-10", "Acme", "Some content", "rev-01");

        assertNotNull(findings);
        assertTrue(findings.isEmpty());
        verifyNoInteractions(sourceRepository, claimRepository, guidelineRepository);
    }

    @Test
    @DisplayName("Should throw AiConfigurationException when AI is enabled but GOOGLE_API_KEY is missing")
    void performAiReview_WhenEnabledWithoutKey_ShouldThrowConfigurationException() {
        geminiProperties.setEnabled(true);
        geminiProperties.setApiKey("");

        assertThrows(AiConfigurationException.class, () ->
                orchestrator.performAiReview("brand-10", "Acme", "Some content", "rev-01"));
    }

    @Test
    @DisplayName("Should scope sources, claims, and guidelines to the requested brand workspace")
    void buildAiRequest_ShouldScopeWorkspaceResources() {
        String brandId = "brand-acme";

        ApprovedSource source1 = new ApprovedSource(brandId, "SOC2 Compliance Report", "AUDIT", "Security Team", "Zero critical findings.", "https://docs.acme.com/soc2");
        source1.setId("src-01");
        when(sourceRepository.findByBrandId(brandId)).thenReturn(List.of(source1));

        ApprovedClaim claim1 = new ApprovedClaim(brandId, "Guarantees 99.99% uptime", "Uptime SLA", "Engineering", "https://status.acme.com", null);
        claim1.setId("clm-01");
        when(claimRepository.findByBrandId(brandId)).thenReturn(List.of(claim1));

        BrandGuideline guideline = new BrandGuideline();
        guideline.setProhibitedExpressions(List.of(new BrandGuideline.ProhibitedTerm("p1", "revolutionary", "CRITICAL", "Overused buzzword", "modern")));
        guideline.setRequiredPhrases(List.of(new BrandGuideline.RequiredPhrase("r1", "LEGAL", "Terms and conditions apply.", true)));
        guideline.setToneRules(List.of(new BrandGuideline.ToneRule("Directness", "Avoid jargon and buzzwords.", "Clear statement", "Buzzword dense")));
        when(guidelineRepository.findFirstByBrandIdAndStatus(brandId, "ACTIVE")).thenReturn(Optional.of(guideline));

        AiReviewRequest request = orchestrator.buildAiRequest(brandId, "Acme Corp", "Revolutionary cloud computing.", "rev-01");

        assertNotNull(request);
        assertEquals(brandId, request.getBrandId());
        assertEquals("Acme Corp", request.getBrandName());

        assertEquals(1, request.getApprovedSources().size());
        assertEquals("src-01", request.getApprovedSources().get(0).getSourceId());
        assertEquals("SOC2 Compliance Report", request.getApprovedSources().get(0).getTitle());

        assertEquals(1, request.getApprovedClaims().size());
        assertEquals("clm-01", request.getApprovedClaims().get(0).getClaimId());

        assertEquals(1, request.getProhibitedExpressions().size());
        assertEquals("revolutionary", request.getProhibitedExpressions().get(0));

        assertEquals(1, request.getRequiredPhrases().size());
        assertTrue(request.getToneGuidelines().contains("Directness"));
    }

    @Test
    @DisplayName("Should successfully map AI result into domain Finding entities with Intent Lock suggestions")
    void performAiReview_WhenEnabledWithKey_ShouldReturnDomainFindings() {
        geminiProperties.setEnabled(true);
        geminiProperties.setApiKey("test-valid-key");

        String brandId = "brand-acme";
        when(sourceRepository.findByBrandId(brandId)).thenReturn(List.of(
                new ApprovedSource(brandId, "Performance Report", "BENCHMARK", "QA", "10k TPS.", "url")
        ));
        when(claimRepository.findByBrandId(brandId)).thenReturn(List.of());
        when(guidelineRepository.findFirstByBrandIdAndStatus(brandId, "ACTIVE")).thenReturn(Optional.empty());

        List<Finding> findings = orchestrator.performAiReview(brandId, "Acme", "Our best-in-class database engine.", "rev-01");

        assertNotNull(findings);
        assertFalse(findings.isEmpty());

        Finding finding = findings.get(0);
        assertTrue(finding.getId().startsWith("ai-find-"));
        assertEquals("BRAND_TONE", finding.getCategory());
        assertEquals("WARNING", finding.getSeverity());
        assertEquals("best-in-class", finding.getExcerpt());
        assertNotNull(finding.getSuggestedCorrections());
        assertEquals("enterprise-grade", finding.getSuggestedCorrections().getLight());
        assertEquals("industry-standard, rigorously tested", finding.getSuggestedCorrections().getBalanced());
    }
}
