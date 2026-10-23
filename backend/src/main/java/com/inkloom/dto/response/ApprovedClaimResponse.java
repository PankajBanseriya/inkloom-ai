package com.inkloom.dto.response;

import com.inkloom.model.ApprovedClaim;
import java.time.Instant;

public class ApprovedClaimResponse {

    private String id;
    private String brandId;
    private String claimText;
    private String supportingSource;
    private String supportingSourceId;
    private String scope;
    private String status;
    private String owner;
    private Instant reviewDate;
    private Instant expiryDate;

    public ApprovedClaimResponse() {}

    public ApprovedClaimResponse(ApprovedClaim claim) {
        this.id = claim.getId();
        this.brandId = claim.getBrandId();
        this.claimText = claim.getClaimText();
        this.supportingSource = claim.getSupportingSource();
        this.supportingSourceId = claim.getSupportingSourceId();
        this.scope = claim.getScope();
        this.status = claim.getStatus();
        this.owner = claim.getOwner();
        this.reviewDate = claim.getReviewDate();
        this.expiryDate = claim.getExpiryDate();
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
}
