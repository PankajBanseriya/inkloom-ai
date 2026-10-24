package com.inkloom.integration.ai.dto;

public class AiSuggestedCorrectionDto {

    private String light;
    private String balanced;
    private String strong;
    private String rationale;
    private boolean preservesIntent = true;
    private String strength = "BALANCED"; // LIGHT, BALANCED, STRONG

    public AiSuggestedCorrectionDto() {}

    public AiSuggestedCorrectionDto(String light, String balanced, String strong, String rationale) {
        this.light = light;
        this.balanced = balanced;
        this.strong = strong;
        this.rationale = rationale;
    }

    public String getLight() { return light; }
    public void setLight(String light) { this.light = light; }

    public String getBalanced() { return balanced; }
    public void setBalanced(String balanced) { this.balanced = balanced; }

    public String getStrong() { return strong; }
    public void setStrong(String strong) { this.strong = strong; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }

    public boolean isPreservesIntent() { return preservesIntent; }
    public void setPreservesIntent(boolean preservesIntent) { this.preservesIntent = preservesIntent; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }
}
