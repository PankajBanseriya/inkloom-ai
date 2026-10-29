package com.inkloom.dto.response;

public class ViolatedGuidelineDto {

    private String ruleId;
    private String ruleName;
    private String category;
    private long violationCount;
    private String severity;
    private String recommendedAction;

    public ViolatedGuidelineDto() {}

    public ViolatedGuidelineDto(String ruleId, String ruleName, String category, long violationCount, String severity, String recommendedAction) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.category = category;
        this.violationCount = violationCount;
        this.severity = severity;
        this.recommendedAction = recommendedAction;
    }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getViolationCount() { return violationCount; }
    public void setViolationCount(long violationCount) { this.violationCount = violationCount; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
}
