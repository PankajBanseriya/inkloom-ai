package com.inkloom.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ReviewCommentRequest {

    @NotBlank(message = "Comment text is required")
    private String commentText;

    public ReviewCommentRequest() {}

    public ReviewCommentRequest(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
}
