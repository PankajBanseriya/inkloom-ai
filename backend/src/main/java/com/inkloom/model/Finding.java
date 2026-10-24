package com.inkloom.model;

public class Finding {

    private String id;
    private String category; // PROHIBITED_TERMINOLOGY, REQUIRED_TERMINOLOGY, BRAND_TONE, UNSUPPORTED_CLAIM, CLAIM_CONFLICT, ACCESSIBILITY, CLARITY
    private String severity; // CRITICAL, WARNING, INFO
    private String evidenceType; // VERIFIED_RULE, SUBJECTIVE_SUGGESTION, UNSUPPORTED_CLAIM
    private String title;
    private String excerpt;
    private String explanation;
    private String supportingDoc;
    private String supportingSourceId;
    private java.util.List<String> referencedSourceIds = new java.util.ArrayList<>();
    private Integer startOffset;
    private Integer endOffset;
    private SuggestedCorrection suggestedCorrections;
    private boolean resolved = false;
    private Double confidence = 1.0;
    private String referencedClaimId;

    public Finding() {}

    public Finding(String id, String category, String severity, String evidenceType, String title,
                   String excerpt, String explanation, String supportingDoc, SuggestedCorrection suggestedCorrections) {
        this.id = id;
        this.category = category;
        this.severity = severity;
        this.evidenceType = evidenceType;
        this.title = title;
        this.excerpt = excerpt;
        this.explanation = explanation;
        this.supportingDoc = supportingDoc;
        this.suggestedCorrections = suggestedCorrections;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getSupportingDoc() { return supportingDoc; }
    public void setSupportingDoc(String supportingDoc) { this.supportingDoc = supportingDoc; }

    public String getSupportingSourceId() { return supportingSourceId; }
    public void setSupportingSourceId(String supportingSourceId) {
        this.supportingSourceId = supportingSourceId;
        if (supportingSourceId != null && !referencedSourceIds.contains(supportingSourceId)) {
            referencedSourceIds.add(supportingSourceId);
        }
    }

    public java.util.List<String> getReferencedSourceIds() { return referencedSourceIds; }
    public void setReferencedSourceIds(java.util.List<String> referencedSourceIds) {
        this.referencedSourceIds = referencedSourceIds != null ? referencedSourceIds : new java.util.ArrayList<>();
    }

    public Integer getStartOffset() { return startOffset; }
    public void setStartOffset(Integer startOffset) { this.startOffset = startOffset; }

    public Integer getEndOffset() { return endOffset; }
    public void setEndOffset(Integer endOffset) { this.endOffset = endOffset; }

    public SuggestedCorrection getSuggestedCorrections() { return suggestedCorrections; }
    public void setSuggestedCorrections(SuggestedCorrection suggestedCorrections) { this.suggestedCorrections = suggestedCorrections; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getReferencedClaimId() { return referencedClaimId; }
    public void setReferencedClaimId(String referencedClaimId) { this.referencedClaimId = referencedClaimId; }
}
