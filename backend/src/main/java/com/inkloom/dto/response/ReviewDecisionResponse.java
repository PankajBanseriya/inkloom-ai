package com.inkloom.dto.response;

import com.inkloom.model.ReviewDecision;
import java.time.Instant;

public class ReviewDecisionResponse {

    private String id;
    private String reviewId;
    private String reviewerUserId;
    private String reviewerName;
    private String action;
    private String decisionNotes;
    private Instant timestamp;

    public ReviewDecisionResponse() {}

    public ReviewDecisionResponse(ReviewDecision decision) {
        this.id = decision.getId();
        this.reviewId = decision.getReviewId();
        this.reviewerUserId = decision.getReviewerUserId();
        this.reviewerName = decision.getReviewerName();
        this.action = decision.getAction();
        this.decisionNotes = decision.getDecisionNotes();
        this.timestamp = decision.getTimestamp();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public String getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(String reviewerUserId) { this.reviewerUserId = reviewerUserId; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDecisionNotes() { return decisionNotes; }
    public void setDecisionNotes(String decisionNotes) { this.decisionNotes = decisionNotes; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
