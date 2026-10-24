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
    private boolean isExpired;
    private boolean isEvidenceMissing;
    private String validationNotes;

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

        boolean expired = "EXPIRED".equalsIgnoreCase(claim.getStatus())
                || (claim.getExpiryDate() != null && claim.getExpiryDate().isBefore(Instant.now()));
        this.isExpired = expired;

        boolean missing = (claim.getSupportingSource() == null || claim.getSupportingSource().trim().isEmpty())
                && (claim.getSupportingSourceId() == null || claim.getSupportingSourceId().trim().isEmpty());
        this.isEvidenceMissing = missing;

        if (expired && missing) {
            this.validationNotes = "Claim has expired and has no supporting source citations.";
        } else if (expired) {
            this.validationNotes = "Claim benchmark has passed its expiry date. Requires re-certification.";
        } else if (missing) {
            this.validationNotes = "Missing evidence source document. Claim cannot be substantiated.";
        } else {
            this.validationNotes = "Verified against active approved source records.";
        }
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
    public boolean isExpired() { return isExpired; }
    public void setExpired(boolean expired) { isExpired = expired; }
    public boolean isEvidenceMissing() { return isEvidenceMissing; }
    public void setEvidenceMissing(boolean evidenceMissing) { isEvidenceMissing = evidenceMissing; }
    public String getValidationNotes() { return validationNotes; }
    public void setValidationNotes(String validationNotes) { this.validationNotes = validationNotes; }
}
