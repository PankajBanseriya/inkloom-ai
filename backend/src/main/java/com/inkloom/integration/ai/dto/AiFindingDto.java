package com.inkloom.integration.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class AiFindingDto {

    private String category; // PROHIBITED_TERMINOLOGY, UNAPPROVED_CLAIM, EXPIRED_CLAIM, MISLEADING_CLAIM, BRAND_TONE, MISSING_DISCLAIMER, ACCESSIBILITY
    private String severity; // CRITICAL, WARNING, ADVISORY
    private String evidenceType; // VERIFIED_RULE, UNSUPPORTED_CLAIM, SUBJECTIVE_SUGGESTION
    private String title;
    private String contentExcerpt;
    private String explanation;
    private Integer startIndex;
    private Integer endIndex;
    private List<String> referencedSourceIds = new ArrayList<>();
    private List<String> referencedClaimIds = new ArrayList<>();
    private AiSuggestedCorrectionDto suggestedCorrections;
    private Double confidence = 1.0;
    private String uncertaintyNotes;

    public AiFindingDto() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentExcerpt() { return contentExcerpt; }
    public void setContentExcerpt(String contentExcerpt) { this.contentExcerpt = contentExcerpt; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getStartIndex() { return startIndex; }
    public void setStartIndex(Integer startIndex) { this.startIndex = startIndex; }

    public Integer getEndIndex() { return endIndex; }
    public void setEndIndex(Integer endIndex) { this.endIndex = endIndex; }

    public List<String> getReferencedSourceIds() { return referencedSourceIds; }
    public void setReferencedSourceIds(List<String> referencedSourceIds) {
        this.referencedSourceIds = referencedSourceIds != null ? referencedSourceIds : new ArrayList<>();
    }

    public List<String> getReferencedClaimIds() { return referencedClaimIds; }
    public void setReferencedClaimIds(List<String> referencedClaimIds) {
        this.referencedClaimIds = referencedClaimIds != null ? referencedClaimIds : new ArrayList<>();
    }

    public AiSuggestedCorrectionDto getSuggestedCorrections() { return suggestedCorrections; }
    public void setSuggestedCorrections(AiSuggestedCorrectionDto suggestedCorrections) { this.suggestedCorrections = suggestedCorrections; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getUncertaintyNotes() { return uncertaintyNotes; }
    public void setUncertaintyNotes(String uncertaintyNotes) { this.uncertaintyNotes = uncertaintyNotes; }
}
