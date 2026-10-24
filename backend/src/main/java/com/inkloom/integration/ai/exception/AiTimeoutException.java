package com.inkloom.integration.ai.exception;

/**
 * Thrown when an AI provider request exceeds the configured timeout threshold.
 */
public class AiTimeoutException extends AiException {

    public AiTimeoutException(String message) {
        super(message);
    }

    public AiTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
