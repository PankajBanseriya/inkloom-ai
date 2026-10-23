package com.inkloom.exception;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String currentStatus, String targetStatus) {
        super(String.format("Invalid review workflow state transition from '%s' to '%s'", currentStatus, targetStatus));
    }
}
