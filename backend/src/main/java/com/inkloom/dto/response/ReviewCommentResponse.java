package com.inkloom.dto.response;

import com.inkloom.model.ReviewComment;
import java.time.Instant;

public class ReviewCommentResponse {

    private String id;
    private String reviewId;
    private String userId;
    private String authorName;
    private String userRole;
    private String commentText;
    private Instant createdAt;

    public ReviewCommentResponse() {}

    public ReviewCommentResponse(ReviewComment comment) {
        this.id = comment.getId();
        this.reviewId = comment.getReviewId();
        this.userId = comment.getUserId();
        this.authorName = comment.getAuthorName();
        this.userRole = comment.getUserRole();
        this.commentText = comment.getCommentText();
        this.createdAt = comment.getCreatedAt();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
