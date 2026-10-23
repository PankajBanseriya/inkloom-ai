package com.inkloom.dto.response;

import com.inkloom.model.Revision;
import java.time.Instant;
import java.util.List;

public class RevisionResponse {

    private String id;
    private String reviewId;
    private int revisionNumber;
    private String content;
    private String authorUserId;
    private String changeSummary;
    private List<String> acceptedFindingIds;
    private String status;
    private Instant createdAt;

    public RevisionResponse() {}

    public RevisionResponse(Revision revision) {
        this.id = revision.getId();
        this.reviewId = revision.getReviewId();
        this.revisionNumber = revision.getRevisionNumber();
        this.content = revision.getContent();
        this.authorUserId = revision.getAuthorUserId();
        this.changeSummary = revision.getChangeSummary();
        this.acceptedFindingIds = revision.getAcceptedFindingIds();
        this.status = revision.getStatus();
        this.createdAt = revision.getCreatedAt();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public int getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(int revisionNumber) { this.revisionNumber = revisionNumber; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(String authorUserId) { this.authorUserId = authorUserId; }
    public String getChangeSummary() { return changeSummary; }
    public void setChangeSummary(String changeSummary) { this.changeSummary = changeSummary; }
    public List<String> getAcceptedFindingIds() { return acceptedFindingIds; }
    public void setAcceptedFindingIds(List<String> acceptedFindingIds) { this.acceptedFindingIds = acceptedFindingIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
