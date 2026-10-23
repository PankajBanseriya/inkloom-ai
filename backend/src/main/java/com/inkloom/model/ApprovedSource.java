package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "approved_sources")
public class ApprovedSource {

    @Id
    private String id;

    @Indexed
    private String brandId;

    private String title;
    private String sourceType = "POLICY"; // POLICY, PRODUCT_SPEC, LEGAL_DISCLOSURE, RESEARCH_REPORT
    private String owner;
    private String approvalStatus = "APPROVED"; // APPROVED, PENDING, DEPRECATED
    private String version = "1.0";
    private String excerpt;
    private String referenceUrl;
    private Instant lastReviewed = Instant.now();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public ApprovedSource() {}

    public ApprovedSource(String brandId, String title, String sourceType, String owner, String excerpt, String referenceUrl) {
        this.brandId = brandId;
        this.title = title;
        this.sourceType = sourceType;
        this.owner = owner;
        this.excerpt = excerpt;
        this.referenceUrl = referenceUrl;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
