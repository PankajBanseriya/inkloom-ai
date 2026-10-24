package com.inkloom.integration.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class AiReviewResult {

    private String summary;
    private Integer overallComplianceScore;
    private List<AiFindingDto> findings = new ArrayList<>();
    private String rawResponseText;
    private String modelUsed;
    private long executionTimeMs;

    public AiReviewResult() {}

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getOverallComplianceScore() { return overallComplianceScore; }
    public void setOverallComplianceScore(Integer overallComplianceScore) { this.overallComplianceScore = overallComplianceScore; }

    public List<AiFindingDto> getFindings() { return findings; }
    public void setFindings(List<AiFindingDto> findings) {
        this.findings = findings != null ? findings : new ArrayList<>();
    }

    public String getRawResponseText() { return rawResponseText; }
    public void setRawResponseText(String rawResponseText) { this.rawResponseText = rawResponseText; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
