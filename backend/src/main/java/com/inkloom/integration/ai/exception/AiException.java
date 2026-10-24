package com.inkloom.integration.ai.exception;

/**
 * Base exception for all AI provider and review engine operations.
 */
public class AiException extends RuntimeException {

    private final String provider;

    public AiException(String message) {
        super(message);
        this.provider = "gemini";
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
        this.provider = "gemini";
    }

    public AiException(String message, String provider, Throwable cause) {
        super(message, cause);
        this.provider = provider != null ? provider : "unknown";
    }

    public String getProvider() {
        return provider;
    }
}
