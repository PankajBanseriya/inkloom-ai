package com.inkloom.integration.ai.orchestration;

import com.inkloom.integration.ai.BrandReviewAiProvider;
import com.inkloom.integration.ai.config.GeminiProperties;
import com.inkloom.integration.ai.dto.*;
import com.inkloom.integration.ai.exception.AiConfigurationException;
import com.inkloom.integration.ai.exception.AiDisabledException;
import com.inkloom.model.*;
import com.inkloom.repository.ApprovedClaimRepository;
import com.inkloom.repository.ApprovedSourceRepository;
import com.inkloom.repository.BrandGuidelineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrator service responsible for scoping brand guidelines, claims, and ground-truth sources
 * before invoking the AI review provider, and mapping structured AI evaluations into domain findings.
 */
@Service
public class ReviewAiOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ReviewAiOrchestrator.class);

    private final BrandReviewAiProvider aiProvider;
    private final GeminiProperties geminiProperties;
    private final BrandGuidelineRepository guidelineRepository;
    private final ApprovedSourceRepository sourceRepository;
    private final ApprovedClaimRepository claimRepository;

    public ReviewAiOrchestrator(
            @Qualifier("geminiReviewAiProvider") BrandReviewAiProvider aiProvider,
            GeminiProperties geminiProperties,
            BrandGuidelineRepository guidelineRepository,
            ApprovedSourceRepository sourceRepository,
            ApprovedClaimRepository claimRepository) {
        this.aiProvider = aiProvider;
        this.geminiProperties = geminiProperties;
        this.guidelineRepository = guidelineRepository;
        this.sourceRepository = sourceRepository;
        this.claimRepository = claimRepository;
    }

    /**
     * Executes AI-assisted brand governance analysis if AI is enabled.
     *
     * @param brandId the workspace ID scoping the approved sources and guidelines
     * @param brandName the name of the brand
     * @param content the raw submitted text
     * @param reviewId the review ID
     * @return list of validated domain {@link Finding} objects, or an empty list if AI is disabled
     */
    public List<Finding> performAiReview(String brandId, String brandName, String content, String reviewId) {
        if (!geminiProperties.isEnabled()) {
            log.debug("AI Review skipped: Gemini AI feature is disabled in configuration.");
            return Collections.emptyList();
        }

        // If enabled but key is missing, report configuration error as specified in requirements
        if (!geminiProperties.hasValidApiKey()) {
            log.error("AI review requested but GOOGLE_API_KEY is not configured.");
            throw new AiConfigurationException("AI review is enabled but Google Gemini API credentials (GOOGLE_API_KEY) are missing.");
        }

        AiReviewRequest request = buildAiRequest(brandId, brandName, content, reviewId);
        AiReviewResult result = aiProvider.analyzeContent(request);

        return mapToDomainFindings(result, brandId);
    }

    /**
     * Builds the scoped AI review request containing only the approved sources, claims, and guidelines
     * belonging to the target brand workspace.
     */
    public AiReviewRequest buildAiRequest(String brandId, String brandName, String content, String reviewId) {
        AiReviewRequest request = new AiReviewRequest();
        request.setReviewId(reviewId);
        request.setBrandId(brandId);
        request.setBrandName(brandName);
        request.setContent(content);

        // Fetch active sources for this brand workspace
        List<ApprovedSource> sources = sourceRepository.findByBrandId(brandId);
        if (sources != null) {
            List<AiApprovedSourceSnippet> sourceSnippets = sources.stream()
                    .map(s -> new AiApprovedSourceSnippet(
                            s.getId(),
                            s.getTitle(),
                            s.getSourceType(),
                            s.getExcerpt()
                    ))
                    .collect(Collectors.toList());
            request.setApprovedSources(sourceSnippets);
        }

        // Fetch claims for this brand workspace
        List<ApprovedClaim> claims = claimRepository.findByBrandId(brandId);
        if (claims != null) {
            List<AiApprovedClaimSnippet> claimSnippets = claims.stream()
                    .map(c -> new AiApprovedClaimSnippet(
                            c.getId(),
                            c.getClaimText(),
                            c.getSupportingSourceId(),
                            c.getScope(),
                            "EXPIRED".equalsIgnoreCase(c.getStatus())
                    ))
                    .collect(Collectors.toList());
            request.setApprovedClaims(claimSnippets);
        }

        // Fetch active brand guidelines
        Optional<BrandGuideline> guidelineOpt = guidelineRepository.findFirstByBrandIdAndStatus(brandId, "ACTIVE");
        if (guidelineOpt.isPresent()) {
            BrandGuideline g = guidelineOpt.get();
            if (g.getProhibitedExpressions() != null) {
                request.setProhibitedExpressions(g.getProhibitedExpressions().stream()
                        .map(BrandGuideline.ProhibitedTerm::getTerm)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
            if (g.getRequiredPhrases() != null) {
                request.setRequiredPhrases(g.getRequiredPhrases().stream()
                        .map(BrandGuideline.RequiredPhrase::getPhrase)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
            if (g.getToneRules() != null && !g.getToneRules().isEmpty()) {
                StringBuilder toneSb = new StringBuilder();
                for (BrandGuideline.ToneRule tr : g.getToneRules()) {
                    toneSb.append(tr.getAttribute()).append(": ").append(tr.getDescription()).append("; ");
                }
                request.setToneGuidelines(toneSb.toString());
            }
        }

        return request;
    }

    /**
     * Transforms validated AI findings into Inkloom domain Finding entities.
     */
    public List<Finding> mapToDomainFindings(AiReviewResult result, String brandId) {
        if (result == null || result.getFindings() == null || result.getFindings().isEmpty()) {
            return Collections.emptyList();
        }

        List<Finding> domainFindings = new ArrayList<>();
        for (AiFindingDto aiFinding : result.getFindings()) {
            Finding finding = new Finding();
            finding.setId("ai-find-" + UUID.randomUUID().toString().substring(0, 8));
            finding.setCategory(aiFinding.getCategory() != null ? aiFinding.getCategory() : "BRAND_TONE");
            finding.setSeverity(aiFinding.getSeverity() != null ? aiFinding.getSeverity() : "WARNING");
            finding.setEvidenceType(aiFinding.getEvidenceType() != null ? aiFinding.getEvidenceType() : "VERIFIED_RULE");
            finding.setTitle(aiFinding.getTitle() != null ? aiFinding.getTitle() : "AI Governance Finding");
            finding.setExcerpt(aiFinding.getContentExcerpt() != null ? aiFinding.getContentExcerpt() : "");
            finding.setExplanation(aiFinding.getExplanation() != null ? aiFinding.getExplanation() : "");
            finding.setStartOffset(aiFinding.getStartIndex() != null ? aiFinding.getStartIndex() : -1);
            finding.setEndOffset(aiFinding.getEndIndex() != null ? aiFinding.getEndIndex() : -1);
            finding.setResolved(false);

            // Connect supporting source ID if present
            if (!aiFinding.getReferencedSourceIds().isEmpty()) {
                finding.setSupportingSourceId(aiFinding.getReferencedSourceIds().get(0));
                finding.setSupportingDoc("Approved Source Citation: " + aiFinding.getReferencedSourceIds().get(0));
            } else if (!aiFinding.getReferencedClaimIds().isEmpty()) {
                finding.setSupportingDoc("Approved Claim Citation: " + aiFinding.getReferencedClaimIds().get(0));
            }

            // Map Suggested Corrections
            if (aiFinding.getSuggestedCorrections() != null) {
                AiSuggestedCorrectionDto sc = aiFinding.getSuggestedCorrections();
                SuggestedCorrection correction = new SuggestedCorrection(
                        sc.getLight(),
                        sc.getBalanced(),
                        sc.getStrong(),
                        sc.getRationale()
                );
                finding.setSuggestedCorrections(correction);
            }

            domainFindings.add(finding);
        }

        return domainFindings;
    }

    public boolean isAiEnabled() {
        return geminiProperties.isEnabled() && geminiProperties.hasValidApiKey();
    }
}
