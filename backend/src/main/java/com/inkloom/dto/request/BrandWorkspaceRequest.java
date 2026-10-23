package com.inkloom.dto.request;

import jakarta.validation.constraints.NotBlank;

public class BrandWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    private String name;

    @NotBlank(message = "Industry is required")
    private String industry;

    private String description;

    public BrandWorkspaceRequest() {}

    public BrandWorkspaceRequest(String name, String industry, String description) {
        this.name = name;
        this.industry = industry;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
