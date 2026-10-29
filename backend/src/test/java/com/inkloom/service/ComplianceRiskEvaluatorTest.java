package com.inkloom.service;

import com.inkloom.model.Finding;
import com.inkloom.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComplianceRiskEvaluatorTest {

    private ComplianceRiskEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ComplianceRiskEvaluator();
    }

    @Test
    @DisplayName("evaluate assigns CRITICAL risk and blocks approval for prohibited terms and contradictions")
    void testCriticalRiskBlocking() {
        Review review = new Review("brand-1", "user-1", "Ad Copy", "Marketing Copy", "Our system is 100% unbreakable.");
        Finding criticalFinding = new Finding();
        criticalFinding.setId("f-1");
        criticalFinding.setSeverity("CRITICAL");
        criticalFinding.setTitle("Prohibited Term: unbreakable");
        criticalFinding.setUncertaintyClassification("CONFIRMED");

        Finding contradictionFinding = new Finding();
        contradictionFinding.setId("f-2");
        contradictionFinding.setSeverity("CRITICAL");
        contradictionFinding.setCategory("CLAIM_CONFLICT");
        contradictionFinding.setUncertaintyClassification("CONTRADICTION");
        contradictionFinding.setTitle("Contradicts 99.995% SLA");

        review.setFindings(List.of(criticalFinding, contradictionFinding));

        ComplianceRiskEvaluator.EvaluationResult result = evaluator.evaluate(review);

        assertEquals("CRITICAL", result.getRiskLevel());
        assertTrue(result.isApprovalBlocked());
        assertTrue(result.getRiskScore() >= 70.0);
        assertEquals(2, result.getBlockingConditions().size());
        assertTrue(result.getBlockingConditions().get(0).contains("Critical Governance Gate"));
    }

    @Test
    @DisplayName("evaluate assigns HIGH risk and blocks approval for unsupported claims and numeric discrepancies")
    void testHighRiskBlocking() {
        Review review = new Review("brand-1", "user-1", "Release", "Press Release", "We achieve 10x throughput.");
        Finding unsupportedFinding = new Finding();
        unsupportedFinding.setId("f-1");
        unsupportedFinding.setSeverity("HIGH");
        unsupportedFinding.setUncertaintyClassification("UNSUPPORTED");
        unsupportedFinding.setTitle("Unsupported claim: 10x throughput");

        Finding reqFinding = new Finding();
        reqFinding.setId("f-2");
        reqFinding.setCategory("REQUIRED_TERMINOLOGY");
        reqFinding.setSeverity("HIGH");
        reqFinding.setTitle("Missing mandatory legal disclosure");

        review.setFindings(List.of(unsupportedFinding, reqFinding));

        ComplianceRiskEvaluator.EvaluationResult result = evaluator.evaluate(review);

        assertEquals("HIGH", result.getRiskLevel());
        assertTrue(result.isApprovalBlocked());
        assertEquals(2, result.getBlockingConditions().size());
    }

    @Test
    @DisplayName("evaluate assigns MEDIUM risk without blocking approval for tone and low-confidence findings")
    void testMediumRiskPermitsApprovalWithJustification() {
        Review review = new Review("brand-1", "user-1", "Blog", "Technical Blog Post", "Casual intro text.");
        Finding toneFinding = new Finding();
        toneFinding.setId("f-1");
        toneFinding.setSeverity("WARNING");
        toneFinding.setCategory("BRAND_TONE");
        toneFinding.setUncertaintyClassification("LOW_CONFIDENCE");
        toneFinding.setTitle("Tone inconsistency: overly informal");

        review.setFindings(List.of(toneFinding));

        ComplianceRiskEvaluator.EvaluationResult result = evaluator.evaluate(review);

        assertEquals("MEDIUM", result.getRiskLevel());
        assertFalse(result.isApprovalBlocked(), "Medium risk allows qualified reviewer sign-off with recorded notes.");
        assertTrue(result.getBlockingConditions().isEmpty());
    }

    @Test
    @DisplayName("evaluate assigns LOW risk when all content is compliant or all findings resolved")
    void testLowRiskWhenCleanOrResolved() {
        Review review = new Review("brand-1", "user-1", "Doc", "Product Documentation", "Enterprise spec verified.");
        Finding resolvedFinding = new Finding();
        resolvedFinding.setId("f-1");
        resolvedFinding.setSeverity("CRITICAL");
        resolvedFinding.setResolved(true); // Resolved via Intent Lock fix

        review.setFindings(List.of(resolvedFinding));

        ComplianceRiskEvaluator.EvaluationResult result = evaluator.evaluate(review);

        assertEquals("LOW", result.getRiskLevel());
        assertFalse(result.isApprovalBlocked());
        assertEquals(0.0, result.getRiskScore());
        assertTrue(result.getBlockingConditions().isEmpty());
    }
}
