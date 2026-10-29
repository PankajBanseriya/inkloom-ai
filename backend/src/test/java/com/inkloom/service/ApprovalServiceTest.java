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

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalServiceTest {

    private Map<String, Review> reviews;
    private List<ReviewComment> savedComments;
    private List<ReviewDecision> savedDecisions;
    private List<AuditEvent> savedAuditEvents;

    private ReviewRepository reviewRepository;
    private ReviewCommentRepository commentRepository;
    private ReviewDecisionRepository decisionRepository;
    private AuditEventRepository auditEventRepository;
    private StateTransitionValidator stateTransitionValidator;
    private ApprovalService approvalService;

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> iface, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{iface}, handler);
    }

    @BeforeEach
    void setUp() {
        reviews = new HashMap<>();
        savedComments = new ArrayList<>();
        savedDecisions = new ArrayList<>();
        savedAuditEvents = new ArrayList<>();

        reviewRepository = proxy(ReviewRepository.class, (p, m, args) -> {
            if ("findById".equals(m.getName())) {
                return Optional.ofNullable(reviews.get(args[0]));
            }
            if ("save".equals(m.getName())) {
                Review r = (Review) args[0];
                reviews.put(r.getId(), r);
                return r;
            }
            return null;
        });

        commentRepository = proxy(ReviewCommentRepository.class, (p, m, args) -> {
            if ("save".equals(m.getName())) {
                ReviewComment c = (ReviewComment) args[0];
                c.setId("comm-" + (savedComments.size() + 1));
                savedComments.add(c);
                return c;
            }
            return null;
        });

        decisionRepository = proxy(ReviewDecisionRepository.class, (p, m, args) -> {
            if ("save".equals(m.getName())) {
                ReviewDecision d = (ReviewDecision) args[0];
                d.setId("dec-" + (savedDecisions.size() + 1));
                savedDecisions.add(d);
                return d;
            }
            return null;
        });

        auditEventRepository = proxy(AuditEventRepository.class, (p, m, args) -> {
            if ("save".equals(m.getName())) {
                AuditEvent a = (AuditEvent) args[0];
                savedAuditEvents.add(a);
                return a;
            }
            return null;
        });

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
        reviews.put("rev-100", review);

        ReviewCommentRequest request = new ReviewCommentRequest("Please verify paragraph 2 claim.");
        ReviewCommentResponse response = approvalService.addComment("rev-100", request, "user-5", "Sarah Legal", "COMPLIANCE_OFFICER");

        assertNotNull(response);
        assertEquals("Please verify paragraph 2 claim.", response.getCommentText());
        assertEquals("Sarah Legal", response.getAuthorName());
        assertEquals(1, savedComments.size());
        assertEquals(1, savedAuditEvents.size());
    }

    @Test
    @DisplayName("makeDecision updates review status to APPROVED when valid")
    void testMakeDecisionApproved() {
        Review review = new Review("brand-1", "author-1", "Whitepaper", "WHITEPAPER", "Draft body");
        review.setId("rev-100");
        review.setStatus("IN_REVIEW");
        review.setActiveRevisionNumber(2);
        reviews.put("rev-100", review);

        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "All brand guidelines verified.");
        ReviewDecisionResponse response = approvalService.makeDecision("rev-100", request, "reviewer-1", "Elena Brand");

        assertNotNull(response);
        assertEquals("APPROVE", response.getAction());
        assertEquals(2, response.getRevisionNumber());
        assertEquals("APPROVED", review.getStatus());
        assertEquals(1, savedDecisions.size());
        assertEquals(1, savedAuditEvents.size());
    }

    @Test
    @DisplayName("makeDecision throws exception on invalid transition")
    void testMakeDecisionInvalidTransition() {
        Review review = new Review("brand-1", "author-1", "Whitepaper", "WHITEPAPER", "Draft body");
        review.setId("rev-100");
        review.setStatus("DRAFT");
        reviews.put("rev-100", review);

        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "Fast tracking");

        assertThrows(InvalidStateTransitionException.class, () ->
                approvalService.makeDecision("rev-100", request, "reviewer-1", "Elena Brand"));
        assertTrue(savedDecisions.isEmpty());
    }

    @Test
    @DisplayName("makeDecision blocks approval when mandatory compliance conditions exist")
    void testMakeDecisionBlockedByComplianceGate() {
        Review review = new Review("brand-1", "author-1", "Whitepaper", "WHITEPAPER", "Draft body");
        review.setId("rev-101");
        review.setStatus("IN_REVIEW");
        review.setApprovalBlocked(true);
        review.setBlockingConditions(List.of("Critical prohibited term 'unbreakable' detected"));
        reviews.put("rev-101", review);

        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "Attempting approval");

        InvalidStateTransitionException ex = assertThrows(InvalidStateTransitionException.class, () ->
                approvalService.makeDecision("rev-101", request, "reviewer-1", "Elena Brand"));

        assertTrue(ex.getMessage().contains("Mandatory compliance blocking conditions exist"));
        assertTrue(savedDecisions.isEmpty());
    }

    @Test
    @DisplayName("makeDecision blocks author from self-approving their own submission (Separation of Duties)")
    void testMakeDecisionSeparationOfDutiesBlocksAuthor() {
        Review review = new Review("brand-1", "author-1", "Whitepaper", "WHITEPAPER", "Draft body");
        review.setId("rev-102");
        review.setStatus("IN_REVIEW");
        review.setApprovalBlocked(false);
        reviews.put("rev-102", review);

        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "Author self-approval");

        InvalidStateTransitionException ex = assertThrows(InvalidStateTransitionException.class, () ->
                approvalService.makeDecision("rev-102", request, "author-1", "Author User", "REVIEWER"));

        assertTrue(ex.getMessage().contains("Separation of duties violation"));
        assertTrue(savedDecisions.isEmpty());
    }

    @Test
    @DisplayName("makeDecision allows admin to approve own submission when exempted")
    void testMakeDecisionAdminPermittedToApproveOwn() {
        Review review = new Review("brand-1", "admin-1", "Whitepaper", "WHITEPAPER", "Draft body");
        review.setId("rev-103");
        review.setStatus("IN_REVIEW");
        review.setApprovalBlocked(false);
        reviews.put("rev-103", review);

        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "Admin emergency approval");
        ReviewDecisionResponse response = approvalService.makeDecision("rev-103", request, "admin-1", "Admin User", "ADMIN");

        assertNotNull(response);
        assertEquals("APPROVED", review.getStatus());
        assertEquals(1, savedDecisions.size());
    }
}
