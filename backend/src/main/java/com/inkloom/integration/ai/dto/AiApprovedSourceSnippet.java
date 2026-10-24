package com.inkloom.integration.ai.dto;

public class AiApprovedSourceSnippet {

    private String sourceId;
    private String title;
    private String sourceDocType;
    private String excerpt;

    public AiApprovedSourceSnippet() {}

    public AiApprovedSourceSnippet(String sourceId, String title, String sourceDocType, String excerpt) {
        this.sourceId = sourceId;
        this.title = title;
        this.sourceDocType = sourceDocType;
        this.excerpt = excerpt;
    }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSourceDocType() { return sourceDocType; }
    public void setSourceDocType(String sourceDocType) { this.sourceDocType = sourceDocType; }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
}
