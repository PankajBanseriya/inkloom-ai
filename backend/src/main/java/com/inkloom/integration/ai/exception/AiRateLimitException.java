package com.inkloom.integration.ai.exception;

/**
 * Thrown when AI provider returns 429 Too Many Requests or quota exhaustion.
 */
public class AiRateLimitException extends AiException {

    public AiRateLimitException(String message) {
        super(message);
    }

    public AiRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
