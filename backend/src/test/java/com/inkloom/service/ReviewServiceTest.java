package com.inkloom.service;

import com.inkloom.dto.request.CorrectionAcceptRequest;
import com.inkloom.dto.request.ReviewSubmissionRequest;
import com.inkloom.dto.response.ReviewResponse;
import com.inkloom.model.*;
import com.inkloom.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RevisionRepository revisionRepository;
    @Mock
    private BrandWorkspaceRepository workspaceRepository;
    @Mock
    private BrandGuidelineRepository guidelineRepository;
    @Mock
    private ApprovedClaimRepository claimRepository;
    @Mock
    private ApprovedSourceRepository sourceRepository;
    @Mock
    private ReviewFeedbackRepository feedbackRepository;
    @Mock
    private AuditEventRepository auditEventRepository;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(
                reviewRepository,
                revisionRepository,
                workspaceRepository,
                guidelineRepository,
                claimRepository,
                sourceRepository,
                feedbackRepository,
                auditEventRepository
        );
    }

    @Test
    @DisplayName("submitReview detects prohibited expressions and computes compliance score")
    void testSubmitReviewWithProhibitedTerms() {
        String brandId = "brand-123";
        when(workspaceRepository.existsById(brandId)).thenReturn(true);

        BrandGuideline guideline = new BrandGuideline(brandId, "v1.0", "user-1");
        guideline.getProhibitedExpressions().add(new BrandGuideline.ProhibitedTerm(
                "term-1", "bulletproof", "CRITICAL", "Absolute guarantee violates compliance", "enterprise-resilient"
        ));
        when(guidelineRepository.findFirstByBrandIdAndStatus(brandId, "ACTIVE")).thenReturn(Optional.of(guideline));

        ApprovedClaim expiredClaim = new ApprovedClaim(brandId, "industry leading latency metrics", "Q1 Benchmark Report", "Global", "Data Team", java.time.Instant.now().minusSeconds(864000));
        expiredClaim.setId("claim-expired-1");
        expiredClaim.setStatus("EXPIRED");
        when(claimRepository.findByBrandId(brandId)).thenReturn(List.of(expiredClaim));

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId("rev-999");
            return r;
        });

        ReviewSubmissionRequest request = new ReviewSubmissionRequest(
                brandId,
                "Press Release Q3",
                "MARKETING_COPY",
                "Our bulletproof cloud delivers industry leading latency metrics across all regions."
        );

        ReviewResponse response = reviewService.submitReview(request, "author-1");

        assertNotNull(response);
        assertEquals("IN_REVIEW", response.getStatus());
        assertEquals(2, response.getFindings().size());
        assertTrue(response.getComplianceScore() < 100.0);
        verify(revisionRepository, times(1)).save(any(Revision.class));
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("acceptCorrection performs surgical text replacement and creates revision")
    void testAcceptCorrectionIntentLock() {
        String reviewId = "rev-999";

        Finding finding = new Finding(
                "find-1",
                "PROHIBITED_TERMINOLOGY",
                "CRITICAL",
                "VERIFIED_RULE",
                "Prohibited: bulletproof",
                "bulletproof",
                "Unsubstantiated guarantee",
                "Guideline v1.0",
                new SuggestedCorrection("enterprise-resilient", "enterprise resilient architecture", "high-grade resilient systems", "Reason")
        );

        Review review = new Review(
                "brand-123",
                "author-1",
                "Marketing Copy",
                "MARKETING_COPY",
                "Our bulletproof cloud platform delivers superior uptime."
        );
        review.setId(reviewId);
        review.getFindings().add(finding);
        review.setComplianceScore(88.0);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        CorrectionAcceptRequest request = new CorrectionAcceptRequest(
                "find-1",
                "TERSE",
                "enterprise-resilient",
                "Accepting brand compliant term"
        );

        ReviewResponse updated = reviewService.acceptCorrection(reviewId, request, "reviewer-1");

        assertNotNull(updated);
        assertEquals("Our enterprise-resilient cloud platform delivers superior uptime.", updated.getCurrentContent());
        assertEquals("Our bulletproof cloud platform delivers superior uptime.", updated.getOriginalContent());
        assertEquals(2, updated.getActiveRevisionNumber());
        assertEquals(100.0, updated.getComplianceScore());

        verify(revisionRepository, times(1)).save(any(Revision.class));
        verify(feedbackRepository, times(1)).save(any(ReviewFeedback.class));
    }
}
