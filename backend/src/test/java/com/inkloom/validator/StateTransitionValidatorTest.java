package com.inkloom.validator;

import com.inkloom.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class StateTransitionValidatorTest {

    private StateTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StateTransitionValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, SUBMITTED",
            "DRAFT, IN_REVIEW",
            "SUBMITTED, IN_REVIEW",
            "SUBMITTED, CHANGES_REQUESTED",
            "SUBMITTED, REJECTED",
            "IN_REVIEW, APPROVED",
            "IN_REVIEW, CHANGES_REQUESTED",
            "IN_REVIEW, REJECTED",
            "CHANGES_REQUESTED, SUBMITTED",
            "CHANGES_REQUESTED, IN_REVIEW",
            "CHANGES_REQUESTED, REJECTED",
            "APPROVED, SUBMITTED",
            "APPROVED, IN_REVIEW",
            "REJECTED, SUBMITTED",
            "REJECTED, IN_REVIEW",
            "IN_REVIEW, IN_REVIEW"
    })
    @DisplayName("Valid transitions should succeed without throwing exception")
    void testValidTransitions(String from, String to) {
        assertDoesNotThrow(() -> validator.validateTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, APPROVED",
            "DRAFT, REJECTED",
            "CHANGES_REQUESTED, APPROVED"
    })
    @DisplayName("Invalid transitions should throw InvalidStateTransitionException")
    void testInvalidTransitions(String from, String to) {
        assertThrows(InvalidStateTransitionException.class, () -> validator.validateTransition(from, to));
    }

    @Test
    @DisplayName("Null transitions throw InvalidStateTransitionException")
    void testNullTransitions() {
        assertThrows(InvalidStateTransitionException.class, () -> validator.validateTransition(null, "APPROVED"));
        assertThrows(InvalidStateTransitionException.class, () -> validator.validateTransition("IN_REVIEW", null));
    }
}
