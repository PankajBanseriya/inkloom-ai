package com.inkloom.integration.ai.exception;

/**
 * Thrown when an AI-powered feature is explicitly invoked while AI features are disabled in configuration.
 */
public class AiDisabledException extends AiException {

    public AiDisabledException(String message) {
        super(message);
    }
}
