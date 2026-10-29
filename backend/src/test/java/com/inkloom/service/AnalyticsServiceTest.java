package com.inkloom.service;

import com.inkloom.dto.response.CategoryHeatmapItemDto;
import com.inkloom.dto.response.WorkspaceAnalyticsResponse;
import com.inkloom.model.Finding;
import com.inkloom.model.Review;
import com.inkloom.model.ReviewDecision;
import com.inkloom.model.ReviewFeedback;
import com.inkloom.repository.BrandWorkspaceRepository;
import com.inkloom.repository.ReviewDecisionRepository;
import com.inkloom.repository.ReviewFeedbackRepository;
import com.inkloom.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsServiceTest {

    private Map<String, List<Review>> reviewsByBrand;
    private Map<String, List<ReviewFeedback>> feedbackByBrand;
    private Set<String> existingWorkspaceIds;
    private List<ReviewDecision> decisions;

    private ReviewRepository reviewRepository;
    private BrandWorkspaceRepository workspaceRepository;
    private ReviewFeedbackRepository feedbackRepository;
    private ReviewDecisionRepository decisionRepository;

    private AnalyticsService analyticsService;

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> iface, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{iface}, handler);
    }

    @BeforeEach
    void setUp() {
        reviewsByBrand = new HashMap<>();
        feedbackByBrand = new HashMap<>();
        existingWorkspaceIds = new HashSet<>();
        decisions = new ArrayList<>();

        workspaceRepository = proxy(BrandWorkspaceRepository.class, (p, m, args) -> {
            if ("existsById".equals(m.getName())) {
                return existingWorkspaceIds.contains(args[0]);
            }
            return false;
        });

        reviewRepository = proxy(ReviewRepository.class, (p, m, args) -> {
            if ("findByBrandIdOrderByCreatedAtDesc".equals(m.getName())) {
                return reviewsByBrand.getOrDefault(args[0], Collections.emptyList());
            }
            return null;
        });

        feedbackRepository = proxy(ReviewFeedbackRepository.class, (p, m, args) -> {
            if ("findByBrandId".equals(m.getName())) {
                return feedbackByBrand.getOrDefault(args[0], Collections.emptyList());
            }
            return null;
        });

        decisionRepository = proxy(ReviewDecisionRepository.class, (p, m, args) -> {
            if ("findByReviewIdIn".equals(m.getName())) {
                return decisions;
            }
            return null;
        });

        analyticsService = new AnalyticsService(
                reviewRepository,
                workspaceRepository,
                feedbackRepository,
                null,
                null,
                decisionRepository
        );
    }

    @Test
    @DisplayName("Returns hasSufficientData=false when no reviews exist in workspace")
    void testInsufficientDataWhenEmpty() {
        String brandId = "brand-empty";
        existingWorkspaceIds.add(brandId);
        reviewsByBrand.put(brandId, Collections.emptyList());

        WorkspaceAnalyticsResponse response = analyticsService.getWorkspaceAnalytics(brandId);

        assertNotNull(response);
        assertFalse(response.isHasSufficientData());
        assertEquals(0, response.getTotalReviews());
        assertEquals(0, response.getIssuesIdentified());
        assertEquals(0, response.getAmbiguityIndex());
        assertTrue(response.getHeatmapMatrix().isEmpty());
        assertNotNull(response.getMetricExplanations());
        assertTrue(response.getMetricExplanations().containsKey("totalFindingsAnalyzed"));
    }

    @Test
    @DisplayName("Calculates real heatmap matrix, repeated phrases, and ambiguity signals from reviews and feedback")
    void testAnalyticsWithRealReviewsAndFeedback() {
        String brandId = "brand-active";
        existingWorkspaceIds.add(brandId);

        Review review1 = new Review(brandId, "user-1", "Press Release 1", "PRESS_RELEASE", "Content 1");
        review1.setId("rev-1");
        Finding finding1 = new Finding();
        finding1.setCategory("PROHIBITED_TERMINOLOGY");
        finding1.setExcerpt("bulletproof cloud");
        Finding finding2 = new Finding();
        finding2.setCategory("UNSUPPORTED_CLAIM");
        finding2.setExcerpt("fastest database in the galaxy");
        review1.setFindings(List.of(finding1, finding2));
        review1.setComplianceScore(85.0);
        review1.setStatus("IN_REVIEW");

        Review review2 = new Review(brandId, "user-2", "Press Release 2", "PRESS_RELEASE", "Content 2");
        review2.setId("rev-2");
        Finding finding3 = new Finding();
        finding3.setCategory("PROHIBITED_TERMINOLOGY");
        finding3.setExcerpt("bulletproof cloud");
        review2.setFindings(List.of(finding3));
        review2.setComplianceScore(95.0);
        review2.setStatus("APPROVED");

        reviewsByBrand.put(brandId, List.of(review1, review2));

        ReviewFeedback fb1 = new ReviewFeedback(brandId, "rev-1", "find-1", "PROHIBITED_TERMINOLOGY", "ACCEPTED_LIGHT", "Complied");
        ReviewFeedback fb2 = new ReviewFeedback(brandId, "rev-2", "find-3", "PROHIBITED_TERMINOLOGY", "ACCEPTED_BALANCED", "Complied");
        feedbackByBrand.put(brandId, List.of(fb1, fb2));

        WorkspaceAnalyticsResponse response = analyticsService.getWorkspaceAnalytics(brandId);

        assertNotNull(response);
        assertTrue(response.isHasSufficientData());
        assertEquals(2, response.getTotalReviews());
        assertEquals(3, response.getIssuesIdentified());
        assertEquals(90.0, response.getAvgComplianceScore()); // (85 + 95)/2
        assertEquals("PROHIBITED_TERMINOLOGY", response.getMostViolatedCategory());

        List<CategoryHeatmapItemDto> matrix = response.getHeatmapMatrix();
        assertEquals(2, matrix.size());

        CategoryHeatmapItemDto prohibitedRow = matrix.stream()
                .filter(m -> "PROHIBITED_TERMINOLOGY".equals(m.getCategory()))
                .findFirst()
                .orElseThrow();
        assertEquals(2, prohibitedRow.getOccurrences());
        assertTrue(prohibitedRow.getRepeatedPhrases().contains("bulletproof cloud"));
        assertEquals("100%", prohibitedRow.getAcceptanceRate()); // 2 accepted, 0 rejected

        CategoryHeatmapItemDto claimRow = matrix.stream()
                .filter(m -> "UNSUPPORTED_CLAIM".equals(m.getCategory()))
                .findFirst()
                .orElseThrow();
        assertEquals("SOURCE_GAP", claimRow.getStatus());
        assertTrue(response.getAmbiguityIndex() >= 1);

        // Verify Continuous Brand Intelligence metrics
        assertEquals(1, response.getResolvedFindingsCount());
        assertEquals(2, response.getUnresolvedFindingsCount());
        assertTrue(response.getAvgTurnaroundHours() >= 0.0);
        assertNotNull(response.getFrequentlyViolatedGuidelines());
        assertEquals(2, response.getFrequentlyViolatedGuidelines().size());
    }
}
