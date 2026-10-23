package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "review_comments")
public class ReviewComment {

    @Id
    private String id;

    @Indexed
    private String reviewId;

    private String userId;
    private String authorName;
    private String userRole;
    private String commentText;
    private Instant createdAt = Instant.now();

    public ReviewComment() {}

    public ReviewComment(String reviewId, String userId, String authorName, String userRole, String commentText) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.authorName = authorName;
        this.userRole = userRole;
        this.commentText = commentText;
        this.createdAt = Instant.now();
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
