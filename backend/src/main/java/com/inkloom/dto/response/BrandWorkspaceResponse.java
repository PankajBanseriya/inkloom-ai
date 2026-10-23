package com.inkloom.dto.response;

import com.inkloom.model.BrandWorkspace;
import java.time.Instant;

public class BrandWorkspaceResponse {

    private String id;
    private String name;
    private String slug;
    private String industry;
    private String description;
    private String ownerUserId;
    private int memberCount;
    private String activeGuidelinesVersion;
    private Instant createdAt;

    public BrandWorkspaceResponse() {}

    public BrandWorkspaceResponse(BrandWorkspace workspace) {
        this.id = workspace.getId();
        this.name = workspace.getName();
        this.slug = workspace.getSlug();
        this.industry = workspace.getIndustry();
        this.description = workspace.getDescription();
        this.ownerUserId = workspace.getOwnerUserId();
        this.memberCount = workspace.getMemberUserIds() != null ? workspace.getMemberUserIds().size() : 0;
        this.activeGuidelinesVersion = workspace.getActiveGuidelinesVersion();
        this.createdAt = workspace.getCreatedAt();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    public String getActiveGuidelinesVersion() { return activeGuidelinesVersion; }
    public void setActiveGuidelinesVersion(String activeGuidelinesVersion) { this.activeGuidelinesVersion = activeGuidelinesVersion; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
