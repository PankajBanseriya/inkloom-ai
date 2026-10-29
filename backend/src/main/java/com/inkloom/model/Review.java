package com.inkloom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Indexed
    private String brandId;

    @Indexed
    private String authorUserId;

    private String title;
    private String contentType; // Marketing Copy, Press Release, Technical Blog Post, Product Documentation, Email Newsletter
    private String originalContent; // Immutable
    private String currentContent; // Updated with accepted revisions
    private String status = "IN_REVIEW"; // DRAFT, IN_REVIEW, CHANGES_REQUESTED, APPROVED, REJECTED
    private Double complianceScore = 100.0;
    private int activeRevisionNumber = 1;
    private List<Finding> findings = new ArrayList<>();
    private String riskLevel = "LOW"; // LOW, MEDIUM, HIGH, CRITICAL
    private Double riskScore = 0.0;
    private List<String> riskReasons = new ArrayList<>();
    private List<String> blockingConditions = new ArrayList<>();
    private boolean approvalBlocked = false;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public Review() {}

    public Review(String brandId, String authorUserId, String title, String contentType, String originalContent) {
        this.brandId = brandId;
        this.authorUserId = authorUserId;
        this.title = title;
        this.contentType = contentType;
        this.originalContent = originalContent;
        this.currentContent = originalContent;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(String authorUserId) { this.authorUserId = authorUserId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
    public String getCurrentContent() { return currentContent; }
    public void setCurrentContent(String currentContent) { this.currentContent = currentContent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getComplianceScore() { return complianceScore; }
    public void setComplianceScore(Double complianceScore) { this.complianceScore = complianceScore; }
    public int getActiveRevisionNumber() { return activeRevisionNumber; }
    public void setActiveRevisionNumber(int activeRevisionNumber) { this.activeRevisionNumber = activeRevisionNumber; }
    public List<Finding> getFindings() { return findings; }
    public void setFindings(List<Finding> findings) { this.findings = findings; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    public List<String> getRiskReasons() { return riskReasons; }
    public void setRiskReasons(List<String> riskReasons) { this.riskReasons = riskReasons != null ? riskReasons : new ArrayList<>(); }
    public List<String> getBlockingConditions() { return blockingConditions; }
    public void setBlockingConditions(List<String> blockingConditions) { this.blockingConditions = blockingConditions != null ? blockingConditions : new ArrayList<>(); }
    public boolean isApprovalBlocked() { return approvalBlocked; }
    public void setApprovalBlocked(boolean approvalBlocked) { this.approvalBlocked = approvalBlocked; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
