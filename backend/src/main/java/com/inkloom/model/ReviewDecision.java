package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "review_decisions")
public class ReviewDecision {

    @Id
    private String id;

    @Indexed
    private String reviewId;

    private String reviewerUserId;
    private String reviewerName;
    private String action; // APPROVE, REJECT, REQUEST_CHANGES
    private String decisionNotes;
    private Instant timestamp = Instant.now();

    public ReviewDecision() {}

    public ReviewDecision(String reviewId, String reviewerUserId, String reviewerName, String action, String decisionNotes) {
        this.reviewId = reviewId;
        this.reviewerUserId = reviewerUserId;
        this.reviewerName = reviewerName;
        this.action = action;
        this.decisionNotes = decisionNotes;
        this.timestamp = Instant.now();
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
