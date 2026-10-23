package com.inkloom.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ReviewSubmissionRequest {

    @NotBlank(message = "Brand ID is required")
    private String brandId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content type is required")
    private String contentType = "Press Release";

    @NotBlank(message = "Content body is required")
    private String content;

    public ReviewSubmissionRequest() {}

    public ReviewSubmissionRequest(String brandId, String title, String contentType, String content) {
        this.brandId = brandId;
        this.title = title;
        this.contentType = contentType;
        this.content = content;
    }

    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
