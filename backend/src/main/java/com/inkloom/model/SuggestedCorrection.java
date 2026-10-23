package com.inkloom.model;

public class SuggestedCorrection {

    private String light;
    private String balanced;
    private String strong;
    private String rationale;

    public SuggestedCorrection() {}

    public SuggestedCorrection(String light, String balanced, String strong, String rationale) {
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
}
