package com.inkloom.dto.response;

import com.inkloom.model.Finding;
import com.inkloom.model.Review;
import java.time.Instant;
import java.util.List;

public class ReviewResponse {

    private String id;
    private String brandId;
    private String authorUserId;
    private String title;
    private String contentType;
    private String originalContent;
    private String currentContent;
    private String status;
    private Double complianceScore;
    private int activeRevisionNumber;
    private int findingsCount;
    private List<Finding> findings;
    private String riskLevel;
    private Double riskScore;
    private List<String> riskReasons;
    private List<String> blockingConditions;
    private boolean approvalBlocked;
    private Instant createdAt;
    private Instant updatedAt;

    public ReviewResponse() {}

    public ReviewResponse(Review review) {
        this.id = review.getId();
        this.brandId = review.getBrandId();
        this.authorUserId = review.getAuthorUserId();
        this.title = review.getTitle();
        this.contentType = review.getContentType();
        this.originalContent = review.getOriginalContent();
        this.currentContent = review.getCurrentContent();
        this.status = review.getStatus();
        this.complianceScore = review.getComplianceScore();
        this.activeRevisionNumber = review.getActiveRevisionNumber();
        this.findings = review.getFindings();
        this.findingsCount = review.getFindings() != null ? review.getFindings().size() : 0;
        this.riskLevel = review.getRiskLevel();
        this.riskScore = review.getRiskScore();
        this.riskReasons = review.getRiskReasons();
        this.blockingConditions = review.getBlockingConditions();
        this.approvalBlocked = review.isApprovalBlocked();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
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
    public int getFindingsCount() { return findingsCount; }
    public void setFindingsCount(int findingsCount) { this.findingsCount = findingsCount; }
    public List<Finding> getFindings() { return findings; }
    public void setFindings(List<Finding> findings) { this.findings = findings; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    public List<String> getRiskReasons() { return riskReasons; }
    public void setRiskReasons(List<String> riskReasons) { this.riskReasons = riskReasons; }
    public List<String> getBlockingConditions() { return blockingConditions; }
    public void setBlockingConditions(List<String> blockingConditions) { this.blockingConditions = blockingConditions; }
    public boolean isApprovalBlocked() { return approvalBlocked; }
    public void setApprovalBlocked(boolean approvalBlocked) { this.approvalBlocked = approvalBlocked; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
