package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "brand_workspaces")
public class BrandWorkspace {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    private String name;
    private String industry;
    private String description;
    private String ownerUserId;
    private List<String> memberUserIds = new ArrayList<>();
    private String activeGuidelinesVersion = "v1.0";
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public BrandWorkspace() {}

    public BrandWorkspace(String name, String slug, String industry, String description, String ownerUserId) {
        this.name = name;
        this.slug = slug;
        this.industry = industry;
        this.description = description;
        this.ownerUserId = ownerUserId;
        this.memberUserIds.add(ownerUserId);
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }

    public List<String> getMemberUserIds() { return memberUserIds; }
    public void setMemberUserIds(List<String> memberUserIds) { this.memberUserIds = memberUserIds; }

    public String getActiveGuidelinesVersion() { return activeGuidelinesVersion; }
    public void setActiveGuidelinesVersion(String activeGuidelinesVersion) { this.activeGuidelinesVersion = activeGuidelinesVersion; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
