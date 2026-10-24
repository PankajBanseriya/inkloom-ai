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

        auditEventRepository.save(new AuditEvent(
                review.getBrandId(),
                reviewerUserId != null ? reviewerUserId : "reviewer-default",
                "DECISION_" + rawAction,
                "Review",
                reviewId,
                "Review transitioned to " + targetStatus + (request.getDecisionNotes() != null ? ": " + request.getDecisionNotes() : "")
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
