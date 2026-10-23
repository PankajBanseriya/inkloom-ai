package com.inkloom.dto.response;

import com.inkloom.model.BrandGuideline;
import java.time.Instant;
import java.util.List;

public class BrandGuidelineResponse {

    private String id;
    private String brandId;
    private String version;
    private String status;
    private List<BrandGuideline.ProhibitedTerm> prohibitedExpressions;
    private List<BrandGuideline.RequiredPhrase> requiredPhrases;
    private List<BrandGuideline.ToneRule> toneRules;
    private Instant updatedAt;

    public BrandGuidelineResponse() {}

    public BrandGuidelineResponse(BrandGuideline guideline) {
        this.id = guideline.getId();
        this.brandId = guideline.getBrandId();
        this.version = guideline.getVersion();
        this.status = guideline.getStatus();
        this.prohibitedExpressions = guideline.getProhibitedExpressions();
        this.requiredPhrases = guideline.getRequiredPhrases();
        this.toneRules = guideline.getToneRules();
        this.updatedAt = guideline.getUpdatedAt();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<BrandGuideline.ProhibitedTerm> getProhibitedExpressions() { return prohibitedExpressions; }
    public void setProhibitedExpressions(List<BrandGuideline.ProhibitedTerm> prohibitedExpressions) { this.prohibitedExpressions = prohibitedExpressions; }
    public List<BrandGuideline.RequiredPhrase> getRequiredPhrases() { return requiredPhrases; }
    public void setRequiredPhrases(List<BrandGuideline.RequiredPhrase> requiredPhrases) { this.requiredPhrases = requiredPhrases; }
    public List<BrandGuideline.ToneRule> getToneRules() { return toneRules; }
    public void setToneRules(List<BrandGuideline.ToneRule> toneRules) { this.toneRules = toneRules; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
