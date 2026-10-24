package com.inkloom.integration.ai.exception;

/**
 * Thrown when AI provider returns 401 Unauthorized or 403 Forbidden on API invocation.
 */
public class AiAuthenticationException extends AiException {

    public AiAuthenticationException(String message) {
        super(message);
    }

    public AiAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
