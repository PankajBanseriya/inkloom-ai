package com.inkloom.service;

import com.inkloom.dto.response.CategoryHeatmapItemDto;
import com.inkloom.dto.response.WorkspaceAnalyticsResponse;
import com.inkloom.model.Finding;
import com.inkloom.model.Review;
import com.inkloom.model.ReviewFeedback;
import com.inkloom.repository.BrandWorkspaceRepository;
import com.inkloom.repository.ReviewFeedbackRepository;
import com.inkloom.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BrandWorkspaceRepository workspaceRepository;
    @Mock
    private ReviewFeedbackRepository feedbackRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(reviewRepository, workspaceRepository, feedbackRepository);
    }

    @Test
    @DisplayName("Returns hasSufficientData=false when no reviews exist in workspace")
    void testInsufficientDataWhenEmpty() {
        String brandId = "brand-empty";
        when(workspaceRepository.existsById(brandId)).thenReturn(true);
        when(reviewRepository.findByBrandIdOrderByCreatedAtDesc(brandId)).thenReturn(Collections.emptyList());

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
        when(workspaceRepository.existsById(brandId)).thenReturn(true);

        Review review1 = new Review(brandId, "user-1", "Press Release 1", "PRESS_RELEASE", "Content 1");
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
        Finding finding3 = new Finding();
        finding3.setCategory("PROHIBITED_TERMINOLOGY");
        finding3.setExcerpt("bulletproof cloud");
        review2.setFindings(List.of(finding3));
        review2.setComplianceScore(95.0);
        review2.setStatus("APPROVED");

        when(reviewRepository.findByBrandIdOrderByCreatedAtDesc(brandId)).thenReturn(List.of(review1, review2));

        ReviewFeedback fb1 = new ReviewFeedback(brandId, "rev-1", "find-1", "PROHIBITED_TERMINOLOGY", "ACCEPTED_LIGHT", "Complied");
        ReviewFeedback fb2 = new ReviewFeedback(brandId, "rev-2", "find-3", "PROHIBITED_TERMINOLOGY", "ACCEPTED_BALANCED", "Complied");
        when(feedbackRepository.findByBrandId(brandId)).thenReturn(List.of(fb1, fb2));

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
    }
}
