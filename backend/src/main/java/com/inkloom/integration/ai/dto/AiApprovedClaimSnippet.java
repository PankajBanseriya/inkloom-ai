package com.inkloom.integration.ai.dto;

public class AiApprovedClaimSnippet {

    private String claimId;
    private String claimText;
    private String supportingSourceId;
    private String scope;
    private boolean expired;

    public AiApprovedClaimSnippet() {}

    public AiApprovedClaimSnippet(String claimId, String claimText, String supportingSourceId, String scope, boolean expired) {
        this.claimId = claimId;
        this.claimText = claimText;
        this.supportingSourceId = supportingSourceId;
        this.scope = scope;
        this.expired = expired;
    }

    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }

    public String getClaimText() { return claimText; }
    public void setClaimText(String claimText) { this.claimText = claimText; }

    public String getSupportingSourceId() { return supportingSourceId; }
    public void setSupportingSourceId(String supportingSourceId) { this.supportingSourceId = supportingSourceId; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
}
