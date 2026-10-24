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

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         RevisionRepository revisionRepository,
                         BrandWorkspaceRepository workspaceRepository,
                         BrandGuidelineRepository guidelineRepository,
                         ApprovedClaimRepository claimRepository,
                         ApprovedSourceRepository sourceRepository,
                         ReviewFeedbackRepository feedbackRepository,
                         AuditEventRepository auditEventRepository,
                         ReviewAiOrchestrator reviewAiOrchestrator) {
        this.reviewRepository = reviewRepository;
        this.revisionRepository = revisionRepository;
        this.workspaceRepository = workspaceRepository;
        this.guidelineRepository = guidelineRepository;
        this.claimRepository = claimRepository;
        this.sourceRepository = sourceRepository;
        this.feedbackRepository = feedbackRepository;
        this.auditEventRepository = auditEventRepository;
        this.reviewAiOrchestrator = reviewAiOrchestrator;
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
                claimRepository, sourceRepository, feedbackRepository, auditEventRepository, null);
    }

    public ReviewResponse submitReview(ReviewSubmissionRequest request, String authorUserId) {
        if (!workspaceRepository.existsById(request.getBrandId())) {
            throw new ResourceNotFoundException("BrandWorkspace", "id", request.getBrandId());
        }

        String content = request.getContent();
        List<Finding> findings = new ArrayList<>();

        // 1. Fetch active guidelines & claims for deterministic evaluation
        BrandGuideline guideline = guidelineRepository.findFirstByBrandIdAndStatus(request.getBrandId(), "ACTIVE")
                .orElse(null);
        List<ApprovedClaim> claims = claimRepository.findByBrandId(request.getBrandId());

        // Check prohibited expressions
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

                        findings.add(new Finding(
                                "find-" + UUID.randomUUID().toString().substring(0, 8),
                                "PROHIBITED_TERMINOLOGY",
                                term.getSeverity() != null ? term.getSeverity() : "CRITICAL",
                                "VERIFIED_RULE",
                                "Prohibited Expression: \"" + term.getTerm() + "\"",
                                matchExcerpt,
                                term.getReason() != null ? term.getReason() : "Use of prohibited term detected.",
                                "Brand Guideline " + guideline.getVersion() + " (Rule ID: " + term.getId() + ")",
                                correction
                        ));
                    }
                }
            }
        }

        // Check required expressions and mandatory legal disclaimers
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
                                "WARNING",
                                "VERIFIED_RULE",
                                "Missing Mandatory Disclosure: \"" + phraseText + "\"",
                                "[End of Content]",
                                "Brand guidelines require including the following mandatory disclosure: \"" + phraseText + "\"",
                                "Brand Guideline " + guideline.getVersion() + " (Rule ID: " + reqPhrase.getId() + ")",
                                correction
                        );
                        reqFinding.setStartOffset(content.length());
                        reqFinding.setEndOffset(content.length());
                        findings.add(reqFinding);
                    }
                }
            }
        }

        // Check for expired claims
        if (claims != null) {
            for (ApprovedClaim claim : claims) {
                if ("EXPIRED".equalsIgnoreCase(claim.getStatus()) && claim.getClaimText() != null) {
                    // Check if key phrases of expired claim appear
                    String[] words = claim.getClaimText().split("\\s+");
                    if (words.length >= 3) {
                        String subPhrase = words[0] + " " + words[1] + " " + words[2];
                        if (content.toLowerCase().contains(subPhrase.toLowerCase())) {
                            SuggestedCorrection correction = new SuggestedCorrection(
                                    "independently benchmarked performance metrics",
                                    "certified enterprise performance metrics meeting North American standards",
                                    "rigorously tested infrastructure with verified industry benchmarks",
                                    "Claim has expired and requires updated re-certification evidence."
                            );

                            findings.add(new Finding(
                                    "find-" + UUID.randomUUID().toString().substring(0, 8),
                                    "UNSUPPORTED_CLAIM",
                                    "CRITICAL",
                                    "UNSUPPORTED_CLAIM",
                                    "Expired Factual Claim: " + subPhrase,
                                    subPhrase,
                                    "This factual claim corresponds to an expired benchmark record. Independent verification indicates missing current evidence.",
                                    "Approved Claims Ledger — Expired Record (ID: " + claim.getId() + ")",
                                    correction
                            ));
                        }
                    }
                }
            }
        }

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

        auditEventRepository.save(new AuditEvent(
                request.getBrandId(),
                authorUserId,
                "REVIEW_SUBMITTED",
                "Review",
                savedReview.getId(),
                "Submitted review: " + savedReview.getTitle() + " (" + findings.size() + " findings)"
        ));

        return new ReviewResponse(savedReview);
    }

    public ReviewResponse getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return new ReviewResponse(review);
    }

    public List<ReviewResponse> getReviewsByBrand(String brandId) {
        return reviewRepository.findByBrandIdOrderByCreatedAtDesc(brandId).stream()
                .map(ReviewResponse::new)
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
        return review.getFindings() != null ? review.getFindings() : Collections.emptyList();
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

        int newRevisionNum = review.getActiveRevisionNumber() + 1;
        review.setActiveRevisionNumber(newRevisionNum);

        // Recalculate score
        long criticalRemaining = review.getFindings().stream().filter(f -> "CRITICAL".equalsIgnoreCase(f.getSeverity())).count();
        long warningRemaining = review.getFindings().stream().filter(f -> "WARNING".equalsIgnoreCase(f.getSeverity())).count();
        double newScore = Math.max(0.0, 100.0 - (criticalRemaining * 12.0) - (warningRemaining * 5.0));
        review.setComplianceScore(Math.round(newScore * 10.0) / 10.0);
        review.setUpdatedAt(Instant.now());

        Review savedReview = reviewRepository.save(review);

        // Save Revision
        Revision revision = new Revision(
                reviewId,
                newRevisionNum,
                updated,
                userId,
                "Applied " + request.getTier() + " Intent Lock fix for: \"" + targetFinding.getExcerpt() + "\""
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

    public RevisionResponse saveRevision(String reviewId, RevisionCreateRequest request, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        review.setCurrentContent(request.getContent());
        int newRevisionNum = review.getActiveRevisionNumber() + 1;
        review.setActiveRevisionNumber(newRevisionNum);
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);

        Revision revision = new Revision(
                reviewId,
                newRevisionNum,
                request.getContent(),
                userId,
                request.getChangeSummary()
        );
        Revision savedRevision = revisionRepository.save(revision);

        auditEventRepository.save(new AuditEvent(
                review.getBrandId(),
                userId,
                "REVISION_SAVED",
                "Revision",
                savedRevision.getId(),
                "Saved revision #" + newRevisionNum + ": " + request.getChangeSummary()
        ));

        return new RevisionResponse(savedRevision);
    }

    public List<RevisionResponse> getRevisionHistory(String reviewId) {
        return revisionRepository.findByReviewIdOrderByRevisionNumberDesc(reviewId).stream()
                .map(RevisionResponse::new)
                .collect(Collectors.toList());
    }
}
