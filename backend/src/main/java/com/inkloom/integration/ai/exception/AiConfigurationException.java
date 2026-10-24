package com.inkloom.integration.ai.exception;

/**
 * Thrown when AI credentials or required configuration settings are missing or malformed.
 */
public class AiConfigurationException extends AiException {

    public AiConfigurationException(String message) {
        super(message);
    }

    public AiConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
