package com.inkloom.integration.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class AiReviewRequest {

    private String reviewId;
    private String brandId;
    private String brandName;
    private String content;
    private List<AiApprovedSourceSnippet> approvedSources = new ArrayList<>();
    private List<AiApprovedClaimSnippet> approvedClaims = new ArrayList<>();
    private List<String> prohibitedExpressions = new ArrayList<>();
    private List<String> requiredPhrases = new ArrayList<>();
    private String toneGuidelines;

    public AiReviewRequest() {}

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<AiApprovedSourceSnippet> getApprovedSources() { return approvedSources; }
    public void setApprovedSources(List<AiApprovedSourceSnippet> approvedSources) {
        this.approvedSources = approvedSources != null ? approvedSources : new ArrayList<>();
    }

    public List<AiApprovedClaimSnippet> getApprovedClaims() { return approvedClaims; }
    public void setApprovedClaims(List<AiApprovedClaimSnippet> approvedClaims) {
        this.approvedClaims = approvedClaims != null ? approvedClaims : new ArrayList<>();
    }

    public List<String> getProhibitedExpressions() { return prohibitedExpressions; }
    public void setProhibitedExpressions(List<String> prohibitedExpressions) {
        this.prohibitedExpressions = prohibitedExpressions != null ? prohibitedExpressions : new ArrayList<>();
    }

    public List<String> getRequiredPhrases() { return requiredPhrases; }
    public void setRequiredPhrases(List<String> requiredPhrases) {
        this.requiredPhrases = requiredPhrases != null ? requiredPhrases : new ArrayList<>();
    }

    public String getToneGuidelines() { return toneGuidelines; }
    public void setToneGuidelines(String toneGuidelines) { this.toneGuidelines = toneGuidelines; }
}
