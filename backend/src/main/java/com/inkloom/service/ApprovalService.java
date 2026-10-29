package com.inkloom.service;

import com.inkloom.dto.request.ReviewCommentRequest;
import com.inkloom.dto.request.ReviewDecisionRequest;
import com.inkloom.dto.response.ReviewCommentResponse;
import com.inkloom.dto.response.ReviewDecisionResponse;
import com.inkloom.exception.ResourceNotFoundException;
import com.inkloom.model.AuditEvent;
import com.inkloom.model.Review;
import com.inkloom.model.ReviewComment;
import com.inkloom.model.ReviewDecision;
import com.inkloom.repository.AuditEventRepository;
import com.inkloom.repository.ReviewCommentRepository;
import com.inkloom.repository.ReviewDecisionRepository;
import com.inkloom.repository.ReviewRepository;
import com.inkloom.validator.StateTransitionValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository commentRepository;
    private final ReviewDecisionRepository decisionRepository;
    private final AuditEventRepository auditEventRepository;
    private final StateTransitionValidator stateTransitionValidator;

    public ApprovalService(ReviewRepository reviewRepository,
                           ReviewCommentRepository commentRepository,
                           ReviewDecisionRepository decisionRepository,
                           AuditEventRepository auditEventRepository,
                           StateTransitionValidator stateTransitionValidator) {
        this.reviewRepository = reviewRepository;
        this.commentRepository = commentRepository;
        this.decisionRepository = decisionRepository;
        this.auditEventRepository = auditEventRepository;
        this.stateTransitionValidator = stateTransitionValidator;
    }

    public ReviewCommentResponse addComment(String reviewId, ReviewCommentRequest request, String userId, String authorName, String role) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        ReviewComment comment = new ReviewComment(
                reviewId,
                userId != null ? userId : "user-default",
                authorName != null ? authorName : "Team Member",
                role != null ? role : "REVIEWER",
                request.getCommentText()
        );
        ReviewComment savedComment = commentRepository.save(comment);

        auditEventRepository.save(new AuditEvent(
                review.getBrandId(),
                userId != null ? userId : "user-default",
                "COMMENT_ADDED",
                "Review",
                reviewId,
                "Added comment by " + (authorName != null ? authorName : "Team Member")
        ));

        return new ReviewCommentResponse(savedComment);
    }

    public List<ReviewCommentResponse> getComments(String reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }
        return commentRepository.findByReviewIdOrderByCreatedAtAsc(reviewId).stream()
                .map(ReviewCommentResponse::new)
                .collect(Collectors.toList());
    }

    public ReviewDecisionResponse makeDecision(String reviewId, ReviewDecisionRequest request, String reviewerUserId, String reviewerName) {
        return makeDecision(reviewId, request, reviewerUserId, reviewerName, "REVIEWER");
    }

    public ReviewDecisionResponse makeDecision(String reviewId, ReviewDecisionRequest request, String reviewerUserId, String reviewerName, String reviewerRole) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        String targetStatus;
        String rawAction = request.getAction().toUpperCase().trim();
        switch (rawAction) {
            case "APPROVE":
            case "APPROVED":
                targetStatus = "APPROVED";
                break;
            case "REQUEST_CHANGES":
            case "CHANGES_REQUESTED":
                targetStatus = "CHANGES_REQUESTED";
                break;
            case "REJECT":
            case "REJECTED":
                targetStatus = "REJECTED";
                break;
            default:
                targetStatus = rawAction;
                break;
        }

        // Feature 2: Compliance Blocking Gates and Separation of Duties Verification
        if ("APPROVED".equals(targetStatus)) {
            // 1. Mandatory compliance conditions check
            if (review.isApprovalBlocked() || (review.getBlockingConditions() != null && !review.getBlockingConditions().isEmpty())) {
                String conditions = (review.getBlockingConditions() != null && !review.getBlockingConditions().isEmpty())
                        ? String.join("; ", review.getBlockingConditions())
                        : "Mandatory compliance conditions remain unresolved.";
                throw new com.inkloom.exception.InvalidStateTransitionException(
                        "Cannot approve review: Mandatory compliance blocking conditions exist: " + conditions
                );
            }

            // 2. Separation of duties check: Authors cannot self-approve their own submissions unless ADMIN
            if (reviewerUserId != null && review.getAuthorUserId() != null
                    && reviewerUserId.trim().equalsIgnoreCase(review.getAuthorUserId().trim())) {
                boolean isAdmin = "ADMIN".equalsIgnoreCase(reviewerRole);
                if (!isAdmin) {
                    throw new com.inkloom.exception.InvalidStateTransitionException(
                            "Separation of duties violation: Authors cannot approve their own submissions. Qualified reviewer sign-off is required."
                    );
                }
            }
        }

        stateTransitionValidator.validateTransition(review.getStatus(), targetStatus);

        review.setStatus(targetStatus);
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);

        ReviewDecision decision = new ReviewDecision(
                reviewId,
                reviewerUserId != null ? reviewerUserId : "reviewer-default",
                reviewerName != null ? reviewerName : "Reviewer",
                rawAction,
                request.getDecisionNotes(),
                review.getActiveRevisionNumber()
        );
        ReviewDecision savedDecision = decisionRepository.save(decision);

        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("action", rawAction);
        metadata.put("revisionNumber", review.getActiveRevisionNumber());
        metadata.put("previousStatus", review.getStatus());
        metadata.put("newStatus", targetStatus);
        metadata.put("notes", request.getDecisionNotes());

        auditEventRepository.save(new AuditEvent(
                review.getBrandId(),
                reviewerUserId != null ? reviewerUserId : "reviewer-default",
                reviewerName != null ? reviewerName : "Reviewer",
                reviewerRole != null ? reviewerRole : "REVIEWER",
                "DECISION_" + rawAction,
                "Review",
                reviewId,
                "Review transitioned to " + targetStatus + (request.getDecisionNotes() != null ? ": " + request.getDecisionNotes() : ""),
                metadata
        ));

        return new ReviewDecisionResponse(savedDecision);
    }

    public List<ReviewDecisionResponse> getDecisions(String reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }
        return decisionRepository.findByReviewIdOrderByTimestampDesc(reviewId).stream()
                .map(ReviewDecisionResponse::new)
                .collect(Collectors.toList());
    }
}
