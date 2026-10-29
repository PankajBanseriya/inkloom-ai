package com.inkloom.service;

import com.inkloom.model.Finding;
import com.inkloom.model.Review;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic Risk Classification and Compliance Gatekeeper.
 * Mandatory blocking conditions are evaluated deterministically; AI scores never override hard compliance gates.
 */
@Component
public class ComplianceRiskEvaluator {

    public static class EvaluationResult {
        private final String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
        private final double riskScore; // 0.0 to 100.0
        private final List<String> riskReasons;
        private final List<String> blockingConditions;
        private final boolean approvalBlocked;

        public EvaluationResult(String riskLevel, double riskScore, List<String> riskReasons,
                                List<String> blockingConditions, boolean approvalBlocked) {
            this.riskLevel = riskLevel;
            this.riskScore = riskScore;
            this.riskReasons = riskReasons != null ? riskReasons : new ArrayList<>();
            this.blockingConditions = blockingConditions != null ? blockingConditions : new ArrayList<>();
            this.approvalBlocked = approvalBlocked;
        }

        public String getRiskLevel() { return riskLevel; }
        public double getRiskScore() { return riskScore; }
        public List<String> getRiskReasons() { return riskReasons; }
        public List<String> getBlockingConditions() { return blockingConditions; }
        public boolean isApprovalBlocked() { return approvalBlocked; }
    }

    public EvaluationResult evaluate(Review review) {
        if (review == null) {
            return new EvaluationResult("LOW", 0.0, new ArrayList<>(), new ArrayList<>(), false);
        }

        List<Finding> findings = review.getFindings() != null ? review.getFindings() : new ArrayList<>();
        List<String> riskReasons = new ArrayList<>();
        List<String> blockingConditions = new ArrayList<>();

        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;

        for (Finding f : findings) {
            if (f.isResolved()) {
                continue; // Resolved findings do not contribute to active risk or blocking gates
            }

            String severity = f.getSeverity() != null ? f.getSeverity().toUpperCase() : "INFO";
            String uncertainty = f.getUncertaintyClassification() != null ? f.getUncertaintyClassification().toUpperCase() : "";
            String category = f.getCategory() != null ? f.getCategory().toUpperCase() : "";

            // 1. Check for CRITICAL violations: direct contradictions, prohibited terms, expired evidence
            if ("CRITICAL".equals(severity) || "CONTRADICTION".equals(uncertainty) || "EXPIRED_EVIDENCE".equals(uncertainty)) {
                criticalCount++;
                String reason = formatFindingReason("CRITICAL", f);
                riskReasons.add(reason);
                blockingConditions.add("Critical Governance Gate: " + (f.getTitle() != null ? f.getTitle() : f.getExcerpt()));
            }
            // 2. Check for HIGH violations: missing mandatory disclosures, unsupported factual claims, numeric mismatches
            else if ("HIGH".equals(severity) || "UNSUPPORTED".equals(uncertainty) || "NUMERIC_DISCREPANCY".equals(uncertainty)
                    || "REQUIRED_TERMINOLOGY".equals(category)) {
                highCount++;
                String reason = formatFindingReason("HIGH", f);
                riskReasons.add(reason);
                blockingConditions.add("Mandatory Disclosure / Grounding Gate: " + (f.getTitle() != null ? f.getTitle() : f.getExcerpt()));
            }
            // 3. Check for MEDIUM violations: tone/style mismatches, low confidence AI findings (< 0.70)
            else if ("WARNING".equals(severity) || "LOW_CONFIDENCE".equals(uncertainty) || "UNCERTAIN_FINDING".equalsIgnoreCase(f.getEvidenceType())) {
                mediumCount++;
                riskReasons.add("Editorial Discretion Required: " + (f.getTitle() != null ? f.getTitle() : f.getExplanation()));
            }
            // 4. Low/Info findings
            else {
                lowCount++;
            }
        }

        // Determine final risk level and blocking gate
        String riskLevel;
        boolean approvalBlocked;
        double rawRiskScore = (criticalCount * 35.0) + (highCount * 20.0) + (mediumCount * 8.0) + (lowCount * 2.0);
        double riskScore = Math.min(100.0, Math.round(rawRiskScore * 10.0) / 10.0);

        if (criticalCount > 0) {
            riskLevel = "CRITICAL";
            approvalBlocked = true;
        } else if (highCount > 0) {
            riskLevel = "HIGH";
            approvalBlocked = true;
        } else if (mediumCount > 0) {
            riskLevel = "MEDIUM";
            approvalBlocked = false; // Medium risk allows reviewer sign-off with recorded justification
        } else {
            riskLevel = "LOW";
            approvalBlocked = false;
            if (riskReasons.isEmpty()) {
                riskReasons.add("All content statements verified against active guidelines and approved claims.");
            }
        }

        return new EvaluationResult(riskLevel, riskScore, riskReasons, blockingConditions, approvalBlocked);
    }

    private String formatFindingReason(String tier, Finding f) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(tier).append("] ");
        if (f.getTitle() != null && !f.getTitle().isEmpty()) {
            sb.append(f.getTitle());
        } else {
            sb.append(f.getCategory());
        }
        if (f.getExcerpt() != null && !f.getExcerpt().isEmpty()) {
            sb.append(" (Excerpt: \"").append(f.getExcerpt()).append("\")");
        }
        return sb.toString();
    }
}
