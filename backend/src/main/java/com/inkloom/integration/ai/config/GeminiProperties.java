package com.inkloom.integration.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Google Gemini AI integration.
 * Resolves settings from application properties or environment variables (e.g. GOOGLE_API_KEY, GEMINI_MODEL).
 */
@ConfigurationProperties(prefix = "inkloom.ai.gemini")
public class GeminiProperties {

    /**
     * Whether the Gemini AI review engine is enabled.
     * Default is false to allow deterministic rules and offline operation.
     */
    private boolean enabled = false;

    /**
     * Google API Key for the Gemini Developer API.
     * Maps to GOOGLE_API_KEY environment variable.
     */
    private String apiKey = "";

    /**
     * Google Gemini Model identifier.
     * Currently supported official production models on Gemini Developer API:
     * - "gemini-2.5-flash" (Default: low latency, high throughput, strong structured reasoning)
     * - "gemini-2.0-flash"
     * - "gemini-1.5-flash"
     */
    private String model = "gemini-2.5-flash";

    /**
     * Request timeout in seconds for Gemini API calls.
     */
    private int timeoutSeconds = 30;

    /**
     * Maximum character count allowed for submitted content before calling AI.
     */
    private int maxInputChars = 50000;

    /**
     * Maximum output tokens allowed in the model's generated response.
     */
    private int maxOutputTokens = 4096;

    /**
     * Temperature parameter (0.0 to 1.0) governing response variance.
     * Defaults to 0.2 to promote deterministic, rigorous brand compliance evaluations.
     */
    private double temperature = 0.2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        // Fall back to direct environment variable if not populated by Spring property injection
        if (apiKey == null || apiKey.trim().isEmpty()) {
            String envKey = System.getenv("GOOGLE_API_KEY");
            if (envKey != null && !envKey.trim().isEmpty()) {
                return envKey.trim();
            }
        }
        return apiKey != null ? apiKey.trim() : "";
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        // Fall back to environment variable GEMINI_MODEL if present
        if (model == null || model.trim().isEmpty()) {
            String envModel = System.getenv("GEMINI_MODEL");
            if (envModel != null && !envModel.trim().isEmpty()) {
                return envModel.trim();
            }
            return "gemini-2.5-flash";
        }
        return model.trim();
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getMaxInputChars() {
        return maxInputChars;
    }

    public void setMaxInputChars(int maxInputChars) {
        this.maxInputChars = maxInputChars;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /**
     * Checks if a valid non-empty API key is available.
     */
    public boolean hasValidApiKey() {
        String key = getApiKey();
        return key != null && !key.trim().isEmpty();
    }
}
