package com.inkloom.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class ApprovedClaimRequest {

    @NotBlank(message = "Claim text is required")
    private String claimText;

    private String supportingSource;
    private String supportingSourceId;

    @NotBlank(message = "Scope is required")
    private String scope;

    private String owner;
    private Instant expiryDate;

    public ApprovedClaimRequest() {}

    public ApprovedClaimRequest(String claimText, String supportingSource, String scope, String owner, Instant expiryDate) {
        this.claimText = claimText;
        this.supportingSource = supportingSource;
        this.scope = scope;
        this.owner = owner;
        this.expiryDate = expiryDate;
    }

    public String getClaimText() { return claimText; }
    public void setClaimText(String claimText) { this.claimText = claimText; }
    public String getSupportingSource() { return supportingSource; }
    public void setSupportingSource(String supportingSource) { this.supportingSource = supportingSource; }
    public String getSupportingSourceId() { return supportingSourceId; }
    public void setSupportingSourceId(String supportingSourceId) { this.supportingSourceId = supportingSourceId; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
}
