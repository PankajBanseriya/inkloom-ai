package com.inkloom.dto.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceAnalyticsResponse {

    private String brandId;
    private long totalReviews;
    private long pendingReviews;
    private long approvedReviews;
    private long changesRequestedReviews;
    private long issuesIdentified;
    private double avgComplianceScore;
    private Map<String, Long> categoryBreakdown = new HashMap<>();
    private boolean hasSufficientData;
    private List<CategoryHeatmapItemDto> heatmapMatrix = new ArrayList<>();
    private int ambiguityIndex;
    private String mostViolatedCategory;
    private Map<String, String> metricExplanations = new HashMap<>();
    private List<CategoryTrendDto> categoryTrends = new ArrayList<>();
    private List<RecentActivityDto> recentActivity = new ArrayList<>();
    private double avgTurnaroundHours;
    private long highRiskPendingCount;
    private long resolvedFindingsCount;
    private long unresolvedFindingsCount;
    private List<ViolatedGuidelineDto> frequentlyViolatedGuidelines = new ArrayList<>();

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
        this.hasSufficientData = totalReviews > 0 || issuesIdentified > 0;
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

    public boolean isHasSufficientData() { return hasSufficientData; }
    public void setHasSufficientData(boolean hasSufficientData) { this.hasSufficientData = hasSufficientData; }
    public List<CategoryHeatmapItemDto> getHeatmapMatrix() { return heatmapMatrix; }
    public void setHeatmapMatrix(List<CategoryHeatmapItemDto> heatmapMatrix) { this.heatmapMatrix = heatmapMatrix; }
    public int getAmbiguityIndex() { return ambiguityIndex; }
    public void setAmbiguityIndex(int ambiguityIndex) { this.ambiguityIndex = ambiguityIndex; }
    public String getMostViolatedCategory() { return mostViolatedCategory; }
    public void setMostViolatedCategory(String mostViolatedCategory) { this.mostViolatedCategory = mostViolatedCategory; }
    public Map<String, String> getMetricExplanations() { return metricExplanations; }
    public void setMetricExplanations(Map<String, String> metricExplanations) { this.metricExplanations = metricExplanations; }
    public List<CategoryTrendDto> getCategoryTrends() { return categoryTrends; }
    public void setCategoryTrends(List<CategoryTrendDto> categoryTrends) { this.categoryTrends = categoryTrends; }
    public List<RecentActivityDto> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<RecentActivityDto> recentActivity) { this.recentActivity = recentActivity; }
    public double getAvgTurnaroundHours() { return avgTurnaroundHours; }
    public void setAvgTurnaroundHours(double avgTurnaroundHours) { this.avgTurnaroundHours = avgTurnaroundHours; }
    public long getHighRiskPendingCount() { return highRiskPendingCount; }
    public void setHighRiskPendingCount(long highRiskPendingCount) { this.highRiskPendingCount = highRiskPendingCount; }
    public long getResolvedFindingsCount() { return resolvedFindingsCount; }
    public void setResolvedFindingsCount(long resolvedFindingsCount) { this.resolvedFindingsCount = resolvedFindingsCount; }
    public long getUnresolvedFindingsCount() { return unresolvedFindingsCount; }
    public void setUnresolvedFindingsCount(long unresolvedFindingsCount) { this.unresolvedFindingsCount = unresolvedFindingsCount; }
    public List<ViolatedGuidelineDto> getFrequentlyViolatedGuidelines() { return frequentlyViolatedGuidelines; }
    public void setFrequentlyViolatedGuidelines(List<ViolatedGuidelineDto> frequentlyViolatedGuidelines) { this.frequentlyViolatedGuidelines = frequentlyViolatedGuidelines; }
}
