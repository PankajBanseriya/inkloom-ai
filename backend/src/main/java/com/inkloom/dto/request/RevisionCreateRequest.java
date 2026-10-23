package com.inkloom.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RevisionCreateRequest {

    @NotBlank(message = "Content body is required")
    private String content;

    @NotBlank(message = "Change summary is required")
    private String changeSummary;

    public RevisionCreateRequest() {}

    public RevisionCreateRequest(String content, String changeSummary) {
        this.content = content;
        this.changeSummary = changeSummary;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getChangeSummary() { return changeSummary; }
    public void setChangeSummary(String changeSummary) { this.changeSummary = changeSummary; }
}
