package com.inkloom.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CorrectionAcceptRequest {

    @NotBlank(message = "Finding ID is required")
    private String findingId;

    @NotBlank(message = "Replacement text is required")
    private String replacementText;

    private String tier = "balanced"; // light, balanced, strong, custom
    private String rationale;

    public CorrectionAcceptRequest() {}

    public CorrectionAcceptRequest(String findingId, String replacementText, String tier) {
        this.findingId = findingId;
        this.replacementText = replacementText;
        this.tier = tier;
    }

    public CorrectionAcceptRequest(String findingId, String tier, String replacementText, String rationale) {
        this.findingId = findingId;
        this.tier = tier;
        this.replacementText = replacementText;
        this.rationale = rationale;
    }

    public String getFindingId() { return findingId; }
    public void setFindingId(String findingId) { this.findingId = findingId; }

    public String getReplacementText() { return replacementText; }
    public void setReplacementText(String replacementText) { this.replacementText = replacementText; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
}
