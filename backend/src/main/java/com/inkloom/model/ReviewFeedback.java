package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "review_feedback")
public class ReviewFeedback {

    @Id
    private String id;

    @Indexed
    private String brandId;

    @Indexed
    private String reviewId;

    private String findingId;
    private String category;
    private String actionTaken; // ACCEPTED_LIGHT, ACCEPTED_BALANCED, ACCEPTED_STRONG, REJECTED, MANUALLY_EDITED
    private String rejectionReason;
    private Instant createdAt = Instant.now();

    public ReviewFeedback() {}

    public ReviewFeedback(String brandId, String reviewId, String findingId, String category, String actionTaken, String rejectionReason) {
        this.brandId = brandId;
        this.reviewId = reviewId;
        this.findingId = findingId;
        this.category = category;
        this.actionTaken = actionTaken;
        this.rejectionReason = rejectionReason;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public String getFindingId() { return findingId; }
    public void setFindingId(String findingId) { this.findingId = findingId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
