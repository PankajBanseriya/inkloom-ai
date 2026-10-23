package com.inkloom.dto.response;

import com.inkloom.model.ApprovedSource;
import java.time.Instant;

public class ApprovedSourceResponse {

    private String id;
    private String brandId;
    private String title;
    private String sourceType;
    private String owner;
    private String approvalStatus;
    private String version;
    private String excerpt;
    private String referenceUrl;
    private Instant lastReviewed;

    public ApprovedSourceResponse() {}

    public ApprovedSourceResponse(ApprovedSource source) {
        this.id = source.getId();
        this.brandId = source.getBrandId();
        this.title = source.getTitle();
        this.sourceType = source.getSourceType();
        this.owner = source.getOwner();
        this.approvalStatus = source.getApprovalStatus();
        this.version = source.getVersion();
        this.excerpt = source.getExcerpt();
        this.referenceUrl = source.getReferenceUrl();
        this.lastReviewed = source.getLastReviewed();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    public String getReferenceUrl() { return referenceUrl; }
    public void setReferenceUrl(String referenceUrl) { this.referenceUrl = referenceUrl; }
    public Instant getLastReviewed() { return lastReviewed; }
    public void setLastReviewed(Instant lastReviewed) { this.lastReviewed = lastReviewed; }
}
