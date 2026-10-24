package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "revisions")
public class Revision {

    @Id
    private String id;

    @Indexed
    private String reviewId;

    private int revisionNumber;
    private Integer sourceRevisionNumber;
    private String content;
    private String authorUserId;
    private String changeSummary;
    private List<String> acceptedFindingIds = new ArrayList<>();
    private String status = "SAVED";
    private Instant createdAt = Instant.now();

    public Revision() {}

    public Revision(String reviewId, int revisionNumber, String content, String authorUserId, String changeSummary) {
        this(reviewId, revisionNumber, content, authorUserId, changeSummary, revisionNumber > 1 ? revisionNumber - 1 : null);
    }

    public Revision(String reviewId, int revisionNumber, String content, String authorUserId, String changeSummary, Integer sourceRevisionNumber) {
        this.reviewId = reviewId;
        this.revisionNumber = revisionNumber;
        this.content = content;
        this.authorUserId = authorUserId;
        this.changeSummary = changeSummary;
        this.sourceRevisionNumber = sourceRevisionNumber;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public int getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(int revisionNumber) { this.revisionNumber = revisionNumber; }
    public Integer getSourceRevisionNumber() { return sourceRevisionNumber; }
    public void setSourceRevisionNumber(Integer sourceRevisionNumber) { this.sourceRevisionNumber = sourceRevisionNumber; }
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
