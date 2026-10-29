package com.inkloom.service;

import com.inkloom.model.ApprovedClaim;
import com.inkloom.model.ApprovedSource;
import com.inkloom.model.BrandGuideline;
import com.inkloom.model.Finding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvidenceGroundingTest {

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(null, null, null, null, null, null, null, null, null, new ComplianceRiskEvaluator());
    }

    @Test
    @DisplayName("runDeterministicChecks detects numeric inconsistency between content and certified claim")
    void testNumericDiscrepancyDetection() {
        String content = "Our enterprise platform provides a 99.9% uptime guarantee with seamless failover.";

        ApprovedClaim claim = new ApprovedClaim();
        claim.setId("claim-sla-01");
        claim.setClaimText("99.995% uptime SLA backed by multi-region active-active cluster");
        claim.setStatus("APPROVED");
        claim.setSupportingSourceId("src-spec-1");

        List<Finding> findings = reviewService.runDeterministicChecks(content, null, List.of(claim), List.of());

        assertFalse(findings.isEmpty());
        Finding numericFinding = findings.stream()
                .filter(f -> "NUMERIC_DISCREPANCY".equalsIgnoreCase(f.getUncertaintyClassification()))
                .findFirst()
                .orElse(null);

        assertNotNull(numericFinding);
        assertEquals("CLAIM_CONFLICT", numericFinding.getCategory());
        assertEquals("CRITICAL", numericFinding.getSeverity());
        assertEquals("claim-sla-01", numericFinding.getReferencedClaimId());
        assertTrue(numericFinding.getTitle().contains("99.9%"));
        assertTrue(numericFinding.getTitle().contains("99.995%"));
    }

    @Test
    @DisplayName("runDeterministicChecks detects scope contradiction when Enterprise claim is promised for all tiers")
    void testScopeContradictionDetection() {
        String content = "Advanced distributed data replication is available to all tiers at no extra charge.";

        ApprovedClaim claim = new ApprovedClaim();
        claim.setId("claim-scope-01");
        claim.setClaimText("Distributed data replication cluster with dedicated consensus nodes");
        claim.setScope("Enterprise tier only");
        claim.setStatus("APPROVED");

        List<Finding> findings = reviewService.runDeterministicChecks(content, null, List.of(claim), List.of());

        assertFalse(findings.isEmpty());
        Finding scopeFinding = findings.stream()
                .filter(f -> "CONTRADICTION".equalsIgnoreCase(f.getUncertaintyClassification()))
                .findFirst()
                .orElse(null);

        assertNotNull(scopeFinding);
        assertEquals("HIGH", scopeFinding.getSeverity());
        assertEquals("CONTRADICTED", scopeFinding.getClaimVerificationStatus());
        assertTrue(scopeFinding.getTitle().contains("Scope Contradiction"));
    }

    @Test
    @DisplayName("runDeterministicChecks detects expired claim and flags as EXPIRED_EVIDENCE with CRITICAL severity")
    void testExpiredClaimDetection() {
        String content = "According to our third-party audit, we achieved top tier throughput.";

        ApprovedClaim claim = new ApprovedClaim();
        claim.setId("claim-exp-01");
        claim.setClaimText("third-party audit benchmark for distributed throughput");
        claim.setStatus("EXPIRED");

        List<Finding> findings = reviewService.runDeterministicChecks(content, null, List.of(claim), List.of());

        assertFalse(findings.isEmpty());
        Finding expFinding = findings.stream()
                .filter(f -> "EXPIRED_EVIDENCE".equalsIgnoreCase(f.getUncertaintyClassification()))
                .findFirst()
                .orElse(null);

        assertNotNull(expFinding);
        assertEquals("CRITICAL", expFinding.getSeverity());
        assertEquals("EXPIRED", expFinding.getClaimVerificationStatus());
        assertTrue(expFinding.getTitle().contains("Expired Factual Claim"));
    }

    @Test
    @DisplayName("runDeterministicChecks flags findings supported by DEPRECATED source as EXPIRED_EVIDENCE")
    void testDeprecatedSourceFlagging() {
        String content = "Our architecture is powered by legacy cluster engines.";

        BrandGuideline.ProhibitedTerm term = new BrandGuideline.ProhibitedTerm();
        term.setId("term-01");
        term.setTerm("legacy cluster");
        term.setSeverity("WARNING");
        term.setReason("Deprecated phrasing");

        BrandGuideline guideline = new BrandGuideline();
        guideline.setProhibitedExpressions(List.of(term));

        ApprovedSource deprecatedSource = new ApprovedSource();
        deprecatedSource.setId("src-dep-1");
        deprecatedSource.setTitle("Historical Architecture Spec v1");
        deprecatedSource.setApprovalStatus("DEPRECATED");

        List<Finding> findings = reviewService.runDeterministicChecks(content, guideline, List.of(), List.of(deprecatedSource));
        assertFalse(findings.isEmpty());

        // Simulate attaching the deprecated source to the finding
        findings.get(0).setSupportingSourceId("src-dep-1");

        // Re-run source verification
        List<Finding> verifiedFindings = reviewService.runDeterministicChecks(content, guideline, List.of(), List.of(deprecatedSource));
        assertNotNull(verifiedFindings);
    }
}
