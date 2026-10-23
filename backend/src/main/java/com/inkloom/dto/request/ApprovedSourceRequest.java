package com.inkloom.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ApprovedSourceRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Source type is required")
    private String sourceType = "POLICY";

    private String owner;
    private String version = "1.0";

    @NotBlank(message = "Document excerpt is required")
    private String excerpt;

    private String referenceUrl;

    public ApprovedSourceRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    public String getReferenceUrl() { return referenceUrl; }
    public void setReferenceUrl(String referenceUrl) { this.referenceUrl = referenceUrl; }
}
