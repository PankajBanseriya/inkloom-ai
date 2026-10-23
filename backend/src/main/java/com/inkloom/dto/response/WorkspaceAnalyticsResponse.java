package com.inkloom.dto.response;

import java.util.Map;

public class WorkspaceAnalyticsResponse {

    private String brandId;
    private long totalReviews;
    private long pendingReviews;
    private long approvedReviews;
    private long changesRequestedReviews;
    private long issuesIdentified;
    private double avgComplianceScore;
    private Map<String, Long> categoryBreakdown;

    public WorkspaceAnalyticsResponse() {}

    public WorkspaceAnalyticsResponse(String brandId, long totalReviews, long pendingReviews, long approvedReviews,
                                      long changesRequestedReviews, long issuesIdentified, double avgComplianceScore,
                                      Map<String, Long> categoryBreakdown) {
        this.brandId = brandId;
        this.totalReviews = totalReviews;
        this.pendingReviews = pendingReviews;
        this.approvedReviews = approvedReviews;
        this.changesRequestedReviews = changesRequestedReviews;
        this.issuesIdentified = issuesIdentified;
        this.avgComplianceScore = avgComplianceScore;
        this.categoryBreakdown = categoryBreakdown;
    }

    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }
    public long getPendingReviews() { return pendingReviews; }
    public void setPendingReviews(long pendingReviews) { this.pendingReviews = pendingReviews; }
    public long getApprovedReviews() { return approvedReviews; }
    public void setApprovedReviews(long approvedReviews) { this.approvedReviews = approvedReviews; }
    public long getChangesRequestedReviews() { return changesRequestedReviews; }
    public void setChangesRequestedReviews(long changesRequestedReviews) { this.changesRequestedReviews = changesRequestedReviews; }
    public long getIssuesIdentified() { return issuesIdentified; }
    public void setIssuesIdentified(long issuesIdentified) { this.issuesIdentified = issuesIdentified; }
    public double getAvgComplianceScore() { return avgComplianceScore; }
    public void setAvgComplianceScore(double avgComplianceScore) { this.avgComplianceScore = avgComplianceScore; }
    public Map<String, Long> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, Long> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
}
