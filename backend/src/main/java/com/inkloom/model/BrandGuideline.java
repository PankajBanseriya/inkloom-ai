package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "brand_guidelines")
public class BrandGuideline {

    @Id
    private String id;

    @Indexed
    private String brandId;

    private String version = "v1.0";
    private String status = "ACTIVE"; // ACTIVE, ARCHIVED
    private List<ProhibitedTerm> prohibitedExpressions = new ArrayList<>();
    private List<RequiredPhrase> requiredPhrases = new ArrayList<>();
    private List<ToneRule> toneRules = new ArrayList<>();
    private WritingPreferences writingPreferences = new WritingPreferences();
    private String updatedByUserId;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public BrandGuideline() {}

    public BrandGuideline(String brandId, String version, String updatedByUserId) {
        this.brandId = brandId;
        this.version = version;
        this.updatedByUserId = updatedByUserId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static class ProhibitedTerm {
        private String id;
        private String term;
        private String severity; // CRITICAL, WARNING, INFO
        private String reason;
        private String suggestedAlternative;

        public ProhibitedTerm() {}

        public ProhibitedTerm(String id, String term, String severity, String reason, String suggestedAlternative) {
            this.id = id;
            this.term = term;
            this.severity = severity;
            this.reason = reason;
            this.suggestedAlternative = suggestedAlternative;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getSuggestedAlternative() { return suggestedAlternative; }
        public void setSuggestedAlternative(String suggestedAlternative) { this.suggestedAlternative = suggestedAlternative; }
    }

    public static class RequiredPhrase {
        private String id;
        private String context;
        private String phrase;
        private boolean mandatory = true;

        public RequiredPhrase() {}

        public RequiredPhrase(String id, String context, String phrase, boolean mandatory) {
            this.id = id;
            this.context = context;
            this.phrase = phrase;
            this.mandatory = mandatory;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        public String getPhrase() { return phrase; }
        public void setPhrase(String phrase) { this.phrase = phrase; }
        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
    }

    public static class ToneRule {
        private String attribute;
        private String description;
        private String doExample;
        private String dontExample;

        public ToneRule() {}

        public ToneRule(String attribute, String description, String doExample, String dontExample) {
            this.attribute = attribute;
            this.description = description;
            this.doExample = doExample;
            this.dontExample = dontExample;
        }

        public String getAttribute() { return attribute; }
        public void setAttribute(String attribute) { this.attribute = attribute; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDoExample() { return doExample; }
        public void setDoExample(String doExample) { this.doExample = doExample; }
        public String getDontExample() { return dontExample; }
        public void setDontExample(String dontExample) { this.dontExample = dontExample; }
    }

    public static class WritingPreferences {
        private int maxSentenceLength = 35;
        private String readingLevel = "Grade 10-12";

        public WritingPreferences() {}

        public int getMaxSentenceLength() { return maxSentenceLength; }
        public void setMaxSentenceLength(int maxSentenceLength) { this.maxSentenceLength = maxSentenceLength; }
        public String getReadingLevel() { return readingLevel; }
        public void setReadingLevel(String readingLevel) { this.readingLevel = readingLevel; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ProhibitedTerm> getProhibitedExpressions() { return prohibitedExpressions; }
    public void setProhibitedExpressions(List<ProhibitedTerm> prohibitedExpressions) { this.prohibitedExpressions = prohibitedExpressions; }
    public List<RequiredPhrase> getRequiredPhrases() { return requiredPhrases; }
    public void setRequiredPhrases(List<RequiredPhrase> requiredPhrases) { this.requiredPhrases = requiredPhrases; }
    public List<ToneRule> getToneRules() { return toneRules; }
    public void setToneRules(List<ToneRule> toneRules) { this.toneRules = toneRules; }
    public WritingPreferences getWritingPreferences() { return writingPreferences; }
    public void setWritingPreferences(WritingPreferences writingPreferences) { this.writingPreferences = writingPreferences; }
    public String getUpdatedByUserId() { return updatedByUserId; }
    public void setUpdatedByUserId(String updatedByUserId) { this.updatedByUserId = updatedByUserId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
