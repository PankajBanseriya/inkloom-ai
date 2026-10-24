package com.inkloom.integration.ai.exception;

/**
 * Thrown on unrecoverable downstream provider errors, malformed responses, or parsing failures.
 */
public class AiProviderException extends AiException {

    public AiProviderException(String message) {
        super(message);
    }

    public AiProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
