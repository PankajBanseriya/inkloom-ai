package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "approved_claims")
public class ApprovedClaim {

    @Id
    private String id;

    @Indexed
    private String brandId;

    private String claimText;
    private String supportingSource;
    private String supportingSourceId;
    private String scope;
    private String status = "APPROVED"; // APPROVED, EXPIRED, UNDER_REVIEW
    private String owner;
    private Instant reviewDate = Instant.now();
    private Instant expiryDate;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public ApprovedClaim() {}

    public ApprovedClaim(String brandId, String claimText, String supportingSource, String scope, String owner, Instant expiryDate) {
        this.brandId = brandId;
        this.claimText = claimText;
        this.supportingSource = supportingSource;
        this.scope = scope;
        this.owner = owner;
        this.expiryDate = expiryDate;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getClaimText() { return claimText; }
    public void setClaimText(String claimText) { this.claimText = claimText; }
    public String getSupportingSource() { return supportingSource; }
    public void setSupportingSource(String supportingSource) { this.supportingSource = supportingSource; }
    public String getSupportingSourceId() { return supportingSourceId; }
    public void setSupportingSourceId(String supportingSourceId) { this.supportingSourceId = supportingSourceId; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public Instant getReviewDate() { return reviewDate; }
    public void setReviewDate(Instant reviewDate) { this.reviewDate = reviewDate; }
    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
