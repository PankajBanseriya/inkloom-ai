package com.inkloom.integration.ai.exception;

/**
 * Thrown when the configured model identifier is unsupported or not found on the provider.
 */
public class AiModelNotFoundException extends AiException {

    private final String model;

    public AiModelNotFoundException(String message, String model) {
        super(message);
        this.model = model;
    }

    public AiModelNotFoundException(String message, String model, Throwable cause) {
        super(message, cause);
        this.model = model;
    }

    public String getModel() {
        return model;
    }
}
