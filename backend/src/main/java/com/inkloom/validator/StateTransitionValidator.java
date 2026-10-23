package com.inkloom.validator;

import com.inkloom.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class StateTransitionValidator {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "DRAFT", Set.of("IN_REVIEW"),
            "IN_REVIEW", Set.of("IN_REVIEW", "CHANGES_REQUESTED", "APPROVED", "REJECTED"),
            "CHANGES_REQUESTED", Set.of("IN_REVIEW", "REJECTED"),
            "APPROVED", Set.of("IN_REVIEW"), // Re-opens on new revision
            "REJECTED", Set.of("IN_REVIEW")  // Re-opens on new revision
    );

    public void validateTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new InvalidStateTransitionException("Review status cannot be null");
        }

        if (currentStatus.equals(targetStatus)) {
            return;
        }

        Set<String> allowed = VALID_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new InvalidStateTransitionException(currentStatus, targetStatus);
        }
    }
}
