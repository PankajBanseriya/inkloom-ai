package com.inkloom.service;

import com.inkloom.dto.request.ReviewCommentRequest;
import com.inkloom.dto.request.ReviewDecisionRequest;
import com.inkloom.dto.response.ReviewCommentResponse;
import com.inkloom.dto.response.ReviewDecisionResponse;
import com.inkloom.exception.InvalidStateTransitionException;
import com.inkloom.model.AuditEvent;
import com.inkloom.model.Review;
import com.inkloom.model.ReviewComment;
import com.inkloom.model.ReviewDecision;
import com.inkloom.repository.AuditEventRepository;
import com.inkloom.repository.ReviewCommentRepository;
import com.inkloom.repository.ReviewDecisionRepository;
import com.inkloom.repository.ReviewRepository;
import com.inkloom.validator.StateTransitionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewCommentRepository commentRepository;
    @Mock
    private ReviewDecisionRepository decisionRepository;
    @Mock
    private AuditEventRepository auditEventRepository;

    private StateTransitionValidator stateTransitionValidator;
    private ApprovalService approvalService;

    @BeforeEach
    void setUp() {
        stateTransitionValidator = new StateTransitionValidator();
        approvalService = new ApprovalService(
                reviewRepository,
                commentRepository,
                decisionRepository,
                auditEventRepository,
                stateTransitionValidator
        );
    }

    @Test
    @DisplayName("addComment saves comment and creates audit record")
    void testAddCommentSuccess() {
        Review review = new Review("brand-1", "author-1", "Whitepaper", "WHITEPAPER", "Draft body");
        review.setId("rev-100");

        when(reviewRepository.findById("rev-100")).thenReturn(Optional.of(review));
        when(commentRepository.save(any(ReviewComment.class))).thenAnswer(inv -> {
            ReviewComment c = inv.getArgument(0);
            c.setId("comm-1");
            return c;
        });

        ReviewCommentRequest request = new ReviewCommentRequest("Please verify paragraph 2 claim.");
        ReviewCommentResponse response = approvalService.addComment("rev-100", request, "user-5", "Sarah Legal", "COMPLIANCE_OFFICER");

        assertNotNull(response);
        assertEquals("Please verify paragraph 2 claim.", response.getCommentText());
        assertEquals("Sarah Legal", response.getAuthorName());
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("makeDecision updates review status to APPROVED when valid")
    void testMakeDecisionApproved() {
        Review review = new Review("brand-1", "author-1", "Whitepaper", "WHITEPAPER", "Draft body");
        review.setId("rev-100");
        review.setStatus("IN_REVIEW");
        review.setActiveRevisionNumber(2);

        when(reviewRepository.findById("rev-100")).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
        when(decisionRepository.save(any(ReviewDecision.class))).thenAnswer(inv -> {
            ReviewDecision d = inv.getArgument(0);
            d.setId("dec-1");
            return d;
        });

        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "All brand guidelines verified.");
        ReviewDecisionResponse response = approvalService.makeDecision("rev-100", request, "reviewer-1", "Elena Brand");

        assertNotNull(response);
        assertEquals("APPROVE", response.getAction());
        assertEquals(2, response.getRevisionNumber());
        assertEquals("APPROVED", review.getStatus());
        verify(reviewRepository, times(1)).save(review);
        verify(decisionRepository, times(1)).save(any(ReviewDecision.class));
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("makeDecision throws exception on invalid transition")
    void testMakeDecisionInvalidTransition() {
        Review review = new Review("brand-1", "author-1", "Whitepaper", "WHITEPAPER", "Draft body");
        review.setId("rev-100");
        review.setStatus("DRAFT");

        when(reviewRepository.findById("rev-100")).thenReturn(Optional.of(review));

        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "Fast tracking");

        assertThrows(InvalidStateTransitionException.class, () ->
                approvalService.makeDecision("rev-100", request, "reviewer-1", "Elena Brand"));
        verify(decisionRepository, never()).save(any());
    }
}
