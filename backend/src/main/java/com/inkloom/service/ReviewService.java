package com.inkloom.service;

import com.inkloom.dto.request.CorrectionAcceptRequest;
import com.inkloom.dto.request.ReviewSubmissionRequest;
import com.inkloom.dto.request.RevisionCreateRequest;
import com.inkloom.dto.response.ReviewResponse;
import com.inkloom.dto.response.RevisionResponse;
import com.inkloom.exception.ResourceNotFoundException;
import com.inkloom.integration.ai.orchestration.ReviewAiOrchestrator;
import com.inkloom.model.*;
import com.inkloom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RevisionRepository revisionRepository;
    private final BrandWorkspaceRepository workspaceRepository;
    private final BrandGuidelineRepository guidelineRepository;
    private final ApprovedClaimRepository claimRepository;
    private final ApprovedSourceRepository sourceRepository;
    private final ReviewFeedbackRepository feedbackRepository;
    private final AuditEventRepository auditEventRepository;
    private final ReviewAiOrchestrator reviewAiOrchestrator;
    private final ComplianceRiskEvaluator riskEvaluator;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         RevisionRepository revisionRepository,
                         BrandWorkspaceRepository workspaceRepository,
                         BrandGuidelineRepository guidelineRepository,
                         ApprovedClaimRepository claimRepository,
                         ApprovedSourceRepository sourceRepository,
                         ReviewFeedbackRepository feedbackRepository,
                         AuditEventRepository auditEventRepository,
                         ReviewAiOrchestrator reviewAiOrchestrator,
                         ComplianceRiskEvaluator riskEvaluator) {
        this.reviewRepository = reviewRepository;
        this.revisionRepository = revisionRepository;
        this.workspaceRepository = workspaceRepository;
        this.guidelineRepository = guidelineRepository;
        this.claimRepository = claimRepository;
        this.sourceRepository = sourceRepository;
        this.feedbackRepository = feedbackRepository;
        this.auditEventRepository = auditEventRepository;
        this.reviewAiOrchestrator = reviewAiOrchestrator;
        this.riskEvaluator = riskEvaluator != null ? riskEvaluator : new ComplianceRiskEvaluator();
    }

    public ReviewService(ReviewRepository reviewRepository,
                         RevisionRepository revisionRepository,
                         BrandWorkspaceRepository workspaceRepository,
                         BrandGuidelineRepository guidelineRepository,
                         ApprovedClaimRepository claimRepository,
                         ApprovedSourceRepository sourceRepository,
                         ReviewFeedbackRepository feedbackRepository,
                         AuditEventRepository auditEventRepository,
                         ReviewAiOrchestrator reviewAiOrchestrator) {
        this(reviewRepository, revisionRepository, workspaceRepository, guidelineRepository,
                claimRepository, sourceRepository, feedbackRepository, auditEventRepository, reviewAiOrchestrator,
                new ComplianceRiskEvaluator());
    }

    public ReviewService(ReviewRepository reviewRepository,
                         RevisionRepository revisionRepository,
                         BrandWorkspaceRepository workspaceRepository,
                         BrandGuidelineRepository guidelineRepository,
                         ApprovedClaimRepository claimRepository,
                         ApprovedSourceRepository sourceRepository,
                         ReviewFeedbackRepository feedbackRepository,
                         AuditEventRepository auditEventRepository) {
        this(reviewRepository, revisionRepository, workspaceRepository, guidelineRepository,
                claimRepository, sourceRepository, feedbackRepository, auditEventRepository, null,
                new ComplianceRiskEvaluator());
    }

    public ReviewResponse submitReview(ReviewSubmissionRequest request, String authorUserId) {
        if (!workspaceRepository.existsById(request.getBrandId())) {
            throw new ResourceNotFoundException("BrandWorkspace", "id", request.getBrandId());
        }

        String content = request.getContent();

        // 1. Fetch active guidelines, claims, and sources for deterministic evaluation
        BrandGuideline guideline = guidelineRepository.findFirstByBrandIdAndStatus(request.getBrandId(), "ACTIVE")
                .orElse(null);
        List<ApprovedClaim> claims = claimRepository.findByBrandId(request.getBrandId());
        List<ApprovedSource> sources = sourceRepository != null ? sourceRepository.findByBrandId(request.getBrandId()) : Collections.emptyList();

        List<Finding> findings = runDeterministicChecks(content, guideline, claims, sources);

        // 2. AI-Assisted Brand Governance Evaluation (if enabled)
        if (reviewAiOrchestrator != null && reviewAiOrchestrator.isAiEnabled()) {
            String brandName = workspaceRepository.findById(request.getBrandId())
                    .map(BrandWorkspace::getName)
                    .orElse(request.getBrandId());
            List<Finding> aiFindings = reviewAiOrchestrator.performAiReview(
                    request.getBrandId(), brandName, content, "pending"
            );
            if (aiFindings != null) {
                for (Finding af : aiFindings) {
                    boolean alreadyFlagged = findings.stream()
                            .anyMatch(df -> df.getExcerpt() != null && !df.getExcerpt().isEmpty()
                                    && df.getExcerpt().equalsIgnoreCase(af.getExcerpt()));
                    if (!alreadyFlagged) {
                        findings.add(af);
                    }
                }
            }
        }

        // Calculate compliance score
        long criticalCount = findings.stream().filter(f -> "CRITICAL".equalsIgnoreCase(f.getSeverity())).count();
        long warningCount = findings.stream().filter(f -> "WARNING".equalsIgnoreCase(f.getSeverity())).count();
        long infoCount = findings.stream().filter(f -> "INFO".equalsIgnoreCase(f.getSeverity())).count();
        double score = Math.max(0.0, 100.0 - (criticalCount * 12.0) - (warningCount * 5.0) - (infoCount * 2.0));

        Review review = new Review(
                request.getBrandId(),
                authorUserId != null ? authorUserId : "author-default",
                request.getTitle(),
                request.getContentType(),
                content
        );
        review.setFindings(findings);
        review.setComplianceScore(Math.round(score * 10.0) / 10.0);
        review.setStatus("IN_REVIEW");

        // Deterministic Risk-Based Compliance Gatekeeper
        ComplianceRiskEvaluator.EvaluationResult evalResult = riskEvaluator.evaluate(review);
        review.setRiskLevel(evalResult.getRiskLevel());
        review.setRiskScore(evalResult.getRiskScore());
        review.setRiskReasons(evalResult.getRiskReasons());
        review.setBlockingConditions(evalResult.getBlockingConditions());
        review.setApprovalBlocked(evalResult.isApprovalBlocked());

        Review savedReview = reviewRepository.save(review);

        // Save Initial Revision #1 (Preserving original content)
        Revision initialRevision = new Revision(
                savedReview.getId(),
                1,
                content,
                authorUserId,
                "Initial document submission for brand governance review."
        );
        revisionRepository.save(initialRevision);

        Map<String, Object> meta = new HashMap<>();
        meta.put("riskLevel", review.getRiskLevel());
        meta.put("approvalBlocked", review.isApprovalBlocked());
        meta.put("findingsCount", findings.size());

        auditEventRepository.save(new AuditEvent(
                request.getBrandId(),
                authorUserId,
                "Author",
                "CONTENT_CREATOR",
                "REVIEW_SUBMITTED",
                "Review",
                savedReview.getId(),
                "Submitted review: " + savedReview.getTitle() + " (" + findings.size() + " findings, Risk: " + review.getRiskLevel() + ")",
                meta
        ));

        return new ReviewResponse(savedReview);
    }

    /**
     * Deterministic compliance evaluation for prohibited terminology, mandatory legal disclosures,
     * expired benchmark evidence, numeric claim discrepancies, and scope contradictions.
     */
    public List<Finding> runDeterministicChecks(String content, BrandGuideline guideline,
                                                List<ApprovedClaim> claims, List<ApprovedSource> sources) {
        List<Finding> findings = new ArrayList<>();
        if (content == null || content.isEmpty()) return findings;

        // 1. Prohibited expressions
        if (guideline != null && guideline.getProhibitedExpressions() != null) {
            for (BrandGuideline.ProhibitedTerm term : guideline.getProhibitedExpressions()) {
                if (term.getTerm() != null && !term.getTerm().isEmpty()) {
                    Pattern pattern = Pattern.compile("\\b" + Pattern.quote(term.getTerm()) + "\\b", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        String matchExcerpt = matcher.group();
                        String alt = term.getSuggestedAlternative() != null ? term.getSuggestedAlternative() : "compliant phrasing";

                        SuggestedCorrection correction = new SuggestedCorrection(
                                alt,
                                "compliant " + alt + " architecture",
                                "robust, enterprise-grade " + alt + " designed for high-availability systems",
                                term.getReason()
                        );

                        Finding finding = new Finding(
                                "find-" + UUID.randomUUID().toString().substring(0, 8),
                                "PROHIBITED_TERMINOLOGY",
                                term.getSeverity() != null ? term.getSeverity() : "CRITICAL",
                                "VERIFIED_RULE",
                                "Prohibited Expression: \"" + term.getTerm() + "\"",
                                matchExcerpt,
                                term.getReason() != null ? term.getReason() : "Use of prohibited term detected.",
                                "Brand Guideline " + guideline.getVersion() + " (Rule ID: " + term.getId() + ")",
                                correction
                        );
                        finding.setUncertaintyClassification("CONFIRMED");
                        finding.setClaimVerificationStatus("CONTRADICTED");
                        findings.add(finding);
                    }
                }
            }
        }

        // 2. Required expressions and mandatory legal disclaimers
        if (guideline != null && guideline.getRequiredPhrases() != null && content.length() > 50) {
            for (BrandGuideline.RequiredPhrase reqPhrase : guideline.getRequiredPhrases()) {
                if (reqPhrase.isMandatory() && reqPhrase.getPhrase() != null && !reqPhrase.getPhrase().trim().isEmpty()) {
                    String phraseText = reqPhrase.getPhrase().trim();
                    if (!content.toLowerCase().contains(phraseText.toLowerCase())) {
                        SuggestedCorrection correction = new SuggestedCorrection(
                                "\n\n" + phraseText,
                                "\n\n" + phraseText,
                                "\n\nLegal Notice: " + phraseText,
                                "Mandatory disclosure required by brand compliance policy: " + reqPhrase.getContext()
                        );

                        Finding reqFinding = new Finding(
                                "find-" + UUID.randomUUID().toString().substring(0, 8),
                                "REQUIRED_TERMINOLOGY",
                                "HIGH",
                                "VERIFIED_RULE",
                                "Missing Mandatory Disclosure: \"" + phraseText + "\"",
                                "[End of Content]",
                                "Brand guidelines require including the following mandatory disclosure: \"" + phraseText + "\"",
                                "Brand Guideline " + guideline.getVersion() + " (Rule ID: " + reqPhrase.getId() + ")",
                                correction
                        );
                        reqFinding.setStartOffset(content.length());
                        reqFinding.setEndOffset(content.length());
                        reqFinding.setUncertaintyClassification("CONFIRMED");
                        reqFinding.setClaimVerificationStatus("UNSUPPORTED");
                        findings.add(reqFinding);
                    }
                }
            }
        }

        // 3. Claims evaluation: expired evidence, contradictions, and numeric inconsistencies
        if (claims != null) {
            for (ApprovedClaim claim : claims) {
                if (claim.getClaimText() == null) continue;

                // Check expired claims
                if ("EXPIRED".equalsIgnoreCase(claim.getStatus())) {
                    String[] words = claim.getClaimText().split("\\s+");
                    String matchPhrase = null;
                    if (words.length >= 2) {
                        String sub2 = words[0] + " " + words[1];
                        if (content.toLowerCase().contains(sub2.toLowerCase())) {
                            matchPhrase = sub2;
                        } else if (words.length >= 3) {
                            String sub3 = words[0] + " " + words[1] + " " + words[2];
                            if (content.toLowerCase().contains(sub3.toLowerCase())) {
                                matchPhrase = sub3;
                            }
                        }
                    }

                    if (matchPhrase != null) {
                        SuggestedCorrection correction = new SuggestedCorrection(
                                "independently benchmarked performance metrics",
                                "certified enterprise performance metrics meeting North American standards",
                                "rigorously tested infrastructure with verified industry benchmarks",
                                "Claim has expired and requires updated re-certification evidence."
                        );

                        Finding expFinding = new Finding(
                                "find-" + UUID.randomUUID().toString().substring(0, 8),
                                "UNSUPPORTED_CLAIM",
                                "CRITICAL",
                                "EXPIRED_EVIDENCE",
                                "Expired Factual Claim: " + matchPhrase,
                                matchPhrase,
                                "This factual claim corresponds to an expired benchmark record (ID: " + claim.getId() + "). Current evidence has not been re-certified.",
                                "Approved Claims Ledger — Expired Record (ID: " + claim.getId() + ")",
                                correction
                        );
                        expFinding.setReferencedClaimId(claim.getId());
                        expFinding.setSupportingSourceId(claim.getSupportingSourceId());
                        expFinding.setUncertaintyClassification("EXPIRED_EVIDENCE");
                        expFinding.setClaimVerificationStatus("EXPIRED");
                        findings.add(expFinding);
                    }
                }
                // Check active approved claims for contradictions and numeric discrepancies
                else if ("APPROVED".equalsIgnoreCase(claim.getStatus())) {
                    // Check numeric discrepancy (e.g. claim certifies 99.995%, content states 99.9% or 100%)
                    Pattern percentPattern = Pattern.compile("(\\b\\d+(?:\\.\\d+)?%)");
                    Matcher claimMatcher = percentPattern.matcher(claim.getClaimText());
                    if (claimMatcher.find()) {
                        String approvedNumber = claimMatcher.group(1);
                        Pattern contentPercentPattern = Pattern.compile("(\\b\\d+(?:\\.\\d+)?%)\\s*(?:uptime|availability|SLA|guarantee|reliability)", Pattern.CASE_INSENSITIVE);
                        Matcher contentMatcher = contentPercentPattern.matcher(content);
                        if (contentMatcher.find()) {
                            String contentNumber = contentMatcher.group(1);
                            if (!contentNumber.equalsIgnoreCase(approvedNumber)) {
                                String excerpt = contentMatcher.group(0);
                                SuggestedCorrection correction = new SuggestedCorrection(
                                        approvedNumber + " uptime SLA",
                                        "industry-standard " + approvedNumber + " uptime SLA",
                                        "contractually guaranteed " + approvedNumber + " high-availability SLA",
                                        "Align metric with Approved Claim #" + claim.getId()
                                );

                                Finding numFinding = new Finding(
                                        "find-" + UUID.randomUUID().toString().substring(0, 8),
                                        "CLAIM_CONFLICT",
                                        "CRITICAL",
                                        "VERIFIED_RULE",
                                        "Numeric Inconsistency: \"" + contentNumber + "\" vs Certified \"" + approvedNumber + "\"",
                                        excerpt,
                                        "The numeric figure (" + contentNumber + ") directly contradicts the certified figure in Approved Claim #" + claim.getId() + " (" + approvedNumber + ").",
                                        "Approved Claim #" + claim.getId() + " (" + claim.getClaimText() + ")",
                                        correction
                                );
                                numFinding.setReferencedClaimId(claim.getId());
                                numFinding.setSupportingSourceId(claim.getSupportingSourceId());
                                numFinding.setUncertaintyClassification("NUMERIC_DISCREPANCY");
                                numFinding.setClaimVerificationStatus("CONTRADICTED");
                                numFinding.setContradictionDetails("Content states " + contentNumber + " but approved claim certifies " + approvedNumber);
                                findings.add(numFinding);
                            }
                        }
                    }

                    // Check scope contradiction (e.g. claim restricted to Enterprise tier only)
                    if (claim.getScope() != null && claim.getScope().toLowerCase().contains("enterprise")) {
                        Pattern scopeConflictPattern = Pattern.compile("\\b(?:available to all tiers|on all plans|available for free|standard plan included|all users)\\b", Pattern.CASE_INSENSITIVE);
                        Matcher scopeMatcher = scopeConflictPattern.matcher(content);
                        if (scopeMatcher.find()) {
                            String excerpt = scopeMatcher.group();
                            SuggestedCorrection correction = new SuggestedCorrection(
                                    "available on Enterprise plans",
                                    "available exclusively with Enterprise tier licensing",
                                    "provisioned for Enterprise tier agreements with dedicated support",
                                    "Scope restricted to Enterprise tier by approved claim."
                            );

                            Finding scopeFinding = new Finding(
                                    "find-" + UUID.randomUUID().toString().substring(0, 8),
                                    "CLAIM_CONFLICT",
                                    "HIGH",
                                    "VERIFIED_RULE",
                                    "Scope Contradiction: Restricted to Enterprise Tier",
                                    excerpt,
                                    "The claim \"" + claim.getClaimText() + "\" is restricted to scope '" + claim.getScope() + "'. Stating broader availability violates approved governance policy.",
                                    "Approved Claim #" + claim.getId() + " (Scope: " + claim.getScope() + ")",
                                    correction
                            );
                            scopeFinding.setReferencedClaimId(claim.getId());
                            scopeFinding.setSupportingSourceId(claim.getSupportingSourceId());
                            scopeFinding.setUncertaintyClassification("CONTRADICTION");
                            scopeFinding.setClaimVerificationStatus("CONTRADICTED");
                            scopeFinding.setContradictionDetails("Claim scope restriction violated.");
                            findings.add(scopeFinding);
                        }
                    }
                }
            }
        }

        // 4. Source Status Verification (Flags deprecated or pending sources as EXPIRED_EVIDENCE)
        if (sources != null && !sources.isEmpty()) {
            Map<String, ApprovedSource> sourceMap = sources.stream()
                    .filter(s -> s.getId() != null)
                    .collect(Collectors.toMap(ApprovedSource::getId, s -> s, (a, b) -> a));

            for (Finding f : findings) {
                if (f.getSupportingSourceId() != null && sourceMap.containsKey(f.getSupportingSourceId())) {
                    ApprovedSource src = sourceMap.get(f.getSupportingSourceId());
                    if ("DEPRECATED".equalsIgnoreCase(src.getApprovalStatus()) || "PENDING".equalsIgnoreCase(src.getApprovalStatus())) {
                        f.setSeverity("CRITICAL");
                        f.setEvidenceType("EXPIRED_EVIDENCE");
                        f.setUncertaintyClassification("EXPIRED_EVIDENCE");
                        f.setExplanation(f.getExplanation() + " Supporting source '" + src.getTitle() + "' has status " + src.getApprovalStatus() + ".");
                    }
                }
            }
        }

        return findings;
    }

    public ReviewResponse getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        sanitizeFindingEvidence(review);
        return new ReviewResponse(review);
    }

    public List<ReviewResponse> getReviewsByBrand(String brandId) {
        return reviewRepository.findByBrandIdOrderByCreatedAtDesc(brandId).stream()
                .map(r -> {
                    sanitizeFindingEvidence(r);
                    return new ReviewResponse(r);
                })
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(ReviewResponse::new)
                .collect(Collectors.toList());
    }

    public List<Finding> getFindings(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        sanitizeFindingEvidence(review);
        return review.getFindings() != null ? review.getFindings() : Collections.emptyList();
    }

    private void sanitizeFindingEvidence(Review review) {
        if (review.getFindings() == null || review.getFindings().isEmpty() || sourceRepository == null) return;
        List<ApprovedSource> authorizedSources = sourceRepository.findByBrandId(review.getBrandId());
        Map<String, ApprovedSource> sourceMap = authorizedSources.stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toMap(ApprovedSource::getId, s -> s, (a, b) -> a));

        for (Finding f : review.getFindings()) {
            if (f.getSupportingSourceId() != null) {
                if (!sourceMap.containsKey(f.getSupportingSourceId())) {
                    f.setSupportingSourceId(null);
                    f.setEvidenceType("UNSUPPORTED_CLAIM");
                    f.setUncertaintyClassification("UNSUPPORTED");
                } else {
                    ApprovedSource src = sourceMap.get(f.getSupportingSourceId());
                    if ("DEPRECATED".equalsIgnoreCase(src.getApprovalStatus()) || "PENDING".equalsIgnoreCase(src.getApprovalStatus())) {
                        f.setSeverity("CRITICAL");
                        f.setEvidenceType("EXPIRED_EVIDENCE");
                        f.setUncertaintyClassification("EXPIRED_EVIDENCE");
                        f.setSupportingDoc("Referenced Source Deprecated/Pending: " + src.getTitle());
                    }
                }
            }
            if (f.getReferencedSourceIds() != null && !f.getReferencedSourceIds().isEmpty()) {
                List<String> validIds = f.getReferencedSourceIds().stream()
                        .filter(sourceMap::containsKey)
                        .collect(Collectors.toList());
                f.setReferencedSourceIds(validIds);
                if (validIds.isEmpty() && "VERIFIED_RULE".equalsIgnoreCase(f.getEvidenceType()) && f.getCategory() != null && f.getCategory().contains("CLAIM")) {
                    f.setEvidenceType("UNSUPPORTED_CLAIM");
                    f.setUncertaintyClassification("UNSUPPORTED");
                    f.setSupportingDoc("Evidence Not Found in Registered Sources");
                }
            }
        }
    }

    public List<SuggestedCorrection> getSuggestions(String reviewId) {
        return getFindings(reviewId).stream()
                .map(Finding::getSuggestedCorrections)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public ReviewResponse acceptCorrection(String reviewId, CorrectionAcceptRequest request, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        Finding targetFinding = review.getFindings().stream()
                .filter(f -> f.getId().equals(request.getFindingId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Finding", "id", request.getFindingId()));

        // Perform surgical replacement in currentContent, preserving originalContent
        String current = review.getCurrentContent();
        String updated = current.replace(targetFinding.getExcerpt(), request.getReplacementText());
        review.setCurrentContent(updated);

        // Mark finding resolved
        targetFinding.setResolved(true);
        review.getFindings().remove(targetFinding);

        int sourceRevisionNum = review.getActiveRevisionNumber();
        int newRevisionNum = sourceRevisionNum + 1;
        review.setActiveRevisionNumber(newRevisionNum);

        // Recalculate score
        long criticalRemaining = review.getFindings().stream().filter(f -> !f.isResolved() && "CRITICAL".equalsIgnoreCase(f.getSeverity())).count();
        long warningRemaining = review.getFindings().stream().filter(f -> !f.isResolved() && "WARNING".equalsIgnoreCase(f.getSeverity())).count();
        double newScore = Math.max(0.0, 100.0 - (criticalRemaining * 12.0) - (warningRemaining * 5.0));
        review.setComplianceScore(Math.round(newScore * 10.0) / 10.0);

        // Re-evaluate risk and compliance gates after correction
        ComplianceRiskEvaluator.EvaluationResult evalResult = riskEvaluator.evaluate(review);
        review.setRiskLevel(evalResult.getRiskLevel());
        review.setRiskScore(evalResult.getRiskScore());
        review.setRiskReasons(evalResult.getRiskReasons());
        review.setBlockingConditions(evalResult.getBlockingConditions());
        review.setApprovalBlocked(evalResult.isApprovalBlocked());
        review.setUpdatedAt(Instant.now());

        Review savedReview = reviewRepository.save(review);

        // Save Revision with source/parent revision lineage
        Revision revision = new Revision(
                reviewId,
                newRevisionNum,
                updated,
                userId,
                "Applied " + request.getTier() + " Intent Lock fix for: \"" + targetFinding.getExcerpt() + "\"",
                sourceRevisionNum
        );
        revision.getAcceptedFindingIds().add(targetFinding.getId());
        revisionRepository.save(revision);

        // Track feedback for Clarity Heatmap intelligence
        feedbackRepository.save(new ReviewFeedback(
                review.getBrandId(),
                reviewId,
                targetFinding.getId(),
                targetFinding.getCategory(),
                "ACCEPTED_" + request.getTier().toUpperCase(),
                request.getRationale()
        ));

        auditEventRepository.save(new AuditEvent(
                review.getBrandId(),
                userId,
                "CORRECTION_ACCEPTED",
                "Review",
                reviewId,
                "Accepted " + request.getTier() + " correction: " + request.getReplacementText()
        ));

        return new ReviewResponse(savedReview);
    }

    public ReviewResponse rejectCorrection(String reviewId, String findingId, String reason, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        Finding targetFinding = review.getFindings().stream()
                .filter(f -> f.getId().equals(findingId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Finding", "id", findingId));

        // Track feedback for Clarity Heatmap intelligence (rejection/friction point)
        feedbackRepository.save(new ReviewFeedback(
                review.getBrandId(),
                reviewId,
                targetFinding.getId(),
                targetFinding.getCategory(),
                "REJECTED",
                reason != null ? reason : "Author rejected suggested correction; original wording preserved."
        ));

        auditEventRepository.save(new AuditEvent(
                review.getBrandId(),
                userId,
                "CORRECTION_REJECTED",
                "Review",
                reviewId,
                "Rejected correction for finding: " + targetFinding.getTitle() + (reason != null ? " (" + reason + ")" : "")
        ));

        return new ReviewResponse(review);
    }

    public RevisionResponse saveRevision(String reviewId, RevisionCreateRequest request, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        review.setCurrentContent(request.getContent());
        int sourceRevNum = review.getActiveRevisionNumber();
        int newRevisionNum = sourceRevNum + 1;
        review.setActiveRevisionNumber(newRevisionNum);

        // Re-evaluate content deterministically against guidelines, claims, and sources
        BrandGuideline guideline = guidelineRepository != null ? guidelineRepository.findFirstByBrandIdAndStatus(review.getBrandId(), "ACTIVE").orElse(null) : null;
        List<ApprovedClaim> claims = claimRepository != null ? claimRepository.findByBrandId(review.getBrandId()) : Collections.emptyList();
        List<ApprovedSource> sources = sourceRepository != null ? sourceRepository.findByBrandId(review.getBrandId()) : Collections.emptyList();
        List<Finding> newFindings = runDeterministicChecks(request.getContent(), guideline, claims, sources);
        review.setFindings(newFindings);

        long criticalCount = newFindings.stream().filter(f -> "CRITICAL".equalsIgnoreCase(f.getSeverity())).count();
        long warningCount = newFindings.stream().filter(f -> "WARNING".equalsIgnoreCase(f.getSeverity())).count();
        double newScore = Math.max(0.0, 100.0 - (criticalCount * 12.0) - (warningCount * 5.0));
        review.setComplianceScore(Math.round(newScore * 10.0) / 10.0);

        ComplianceRiskEvaluator.EvaluationResult evalResult = riskEvaluator.evaluate(review);
        review.setRiskLevel(evalResult.getRiskLevel());
        review.setRiskScore(evalResult.getRiskScore());
        review.setRiskReasons(evalResult.getRiskReasons());
        review.setBlockingConditions(evalResult.getBlockingConditions());
        review.setApprovalBlocked(evalResult.isApprovalBlocked());
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);

        Revision revision = new Revision(
                reviewId,
                newRevisionNum,
                request.getContent(),
                userId,
                request.getChangeSummary(),
                sourceRevNum
        );
        Revision savedRevision = revisionRepository.save(revision);

        Map<String, Object> meta = new HashMap<>();
        meta.put("revisionNumber", newRevisionNum);
        meta.put("riskLevel", review.getRiskLevel());
        meta.put("complianceScore", review.getComplianceScore());

        auditEventRepository.save(new AuditEvent(
                review.getBrandId(),
                userId,
                "Author",
                "CONTENT_CREATOR",
                "REVISION_SAVED",
                "Revision",
                savedRevision.getId(),
                "Saved revision #" + newRevisionNum + ": " + request.getChangeSummary() + " (Risk: " + review.getRiskLevel() + ")",
                meta
        ));

        return new RevisionResponse(savedRevision);
    }

    public List<RevisionResponse> getRevisionHistory(String reviewId) {
        return revisionRepository.findByReviewIdOrderByRevisionNumberDesc(reviewId).stream()
                .map(RevisionResponse::new)
                .collect(Collectors.toList());
    }
}
