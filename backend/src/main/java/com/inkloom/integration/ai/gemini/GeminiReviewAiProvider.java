package com.inkloom.integration.ai.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.inkloom.integration.ai.BrandReviewAiProvider;
import com.inkloom.integration.ai.config.GeminiProperties;
import com.inkloom.integration.ai.dto.*;
import com.inkloom.integration.ai.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Production implementation of {@link BrandReviewAiProvider} using the official Google Gen AI Java SDK
 * to invoke Google Gemini models (e.g., gemini-2.5-flash) via the Gemini Developer API.
 */
@Service
public class GeminiReviewAiProvider implements BrandReviewAiProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiReviewAiProvider.class);

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;

    public GeminiReviewAiProvider(GeminiProperties properties,
                                  @Qualifier("aiObjectMapper") ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    @Override
    public String getProviderName() {
        return "Google Gemini Developer API";
    }

    @Override
    public String getModelName() {
        return properties.getModel();
    }

    @Override
    public AiReviewResult analyzeContent(AiReviewRequest request) {
        // 1. Pre-execution configuration and status checks
        if (!properties.isEnabled()) {
            throw new AiDisabledException("Gemini AI review engine is disabled in configuration.");
        }

        if (!properties.hasValidApiKey()) {
            log.error("AI invocation rejected: GOOGLE_API_KEY is not configured.");
            throw new AiConfigurationException(
                    "Google Gemini API key is missing or blank. Configure the GOOGLE_API_KEY environment variable or inkloom.ai.gemini.api-key property."
            );
        }

        if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            AiReviewResult emptyResult = new AiReviewResult();
            emptyResult.setSummary("No content supplied for evaluation.");
            emptyResult.setOverallComplianceScore(100);
            emptyResult.setModelUsed(properties.getModel());
            return emptyResult;
        }

        if (request.getContent().length() > properties.getMaxInputChars()) {
            throw new AiProviderException(
                    "Submitted content exceeds maximum supported AI input limit of " + properties.getMaxInputChars() + " characters."
            );
        }

        Instant startTime = Instant.now();
        String activeModel = properties.getModel();
        log.info("Dispatching brand governance review for brand '{}' using Gemini model '{}'",
                request.getBrandName() != null ? request.getBrandName() : request.getBrandId(),
                activeModel);

        // 2. Build structured prompt and context
        String prompt = buildPrompt(request);

        // 3. Execute request against Google Gemini Developer API
        String rawResponseText;
        try (Client client = createClient()) {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .temperature((float) properties.getTemperature())
                    .maxOutputTokens(properties.getMaxOutputTokens())
                    .build();

            GenerateContentResponse response = client.models.generateContent(activeModel, prompt, config);

            if (response == null || response.text() == null || response.text().trim().isEmpty()) {
                throw new AiProviderException("Google Gemini returned an empty response payload.");
            }

            rawResponseText = response.text().trim();
        } catch (ApiException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw new AiTimeoutException("Gemini review request timed out after " + properties.getTimeoutSeconds() + "s.", e);
            }
            handleApiException(e, activeModel);
            throw new AiProviderException("Unexpected error during Gemini execution", e);
        } catch (AiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unhandled error invoking Gemini AI provider: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw new AiTimeoutException("Gemini review request timed out after " + properties.getTimeoutSeconds() + "s.", e);
            }
            throw new AiProviderException("Failed to execute Gemini AI review: " + e.getMessage(), e);
        }

        long executionTimeMs = Duration.between(startTime, Instant.now()).toMillis();
        log.info("Gemini review completed successfully in {} ms", executionTimeMs);

        // 4. Parse and validate the structured response against ground-truth evidence
        AiReviewResult result = parseAndValidateResponse(rawResponseText, request);
        result.setModelUsed(activeModel);
        result.setExecutionTimeMs(executionTimeMs);
        result.setRawResponseText(rawResponseText);

        return result;
    }

    /**
     * Initializes a Google Gen AI Client using the configured API key and HTTP timeout options.
     */
    protected Client createClient() {
        HttpOptions httpOptions = HttpOptions.builder()
                .timeout(properties.getTimeoutSeconds() * 1000)
                .build();

        return Client.builder()
                .apiKey(properties.getApiKey())
                .httpOptions(httpOptions)
                .build();
    }

    /**
     * Constructs a rigorous prompt strictly instructing Gemini to inspect content against
     * the scoped brand guidelines, approved claims, and approved ground-truth sources.
     */
    private String buildPrompt(AiReviewRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are Inkloom AI, an Enterprise Brand Governance & Claims Verification Engine.\n");
        sb.append("Analyze the submitted text against the organization's official brand guidelines, approved claims ledger, and verified ground-truth sources.\n\n");

        sb.append("=== ZERO-HALLUCINATION EVIDENCE RULES ===\n");
        sb.append("1. Evidence citations MUST strictly be drawn from the APPROVED SOURCES list below.\n");
        sb.append("2. Under NO circumstances invent, infer, or hallucinate source identifiers.\n");
        sb.append("3. If a factual claim in the content is not substantiated by an approved source, mark it as 'UNAPPROVED_CLAIM' with empty referencedSourceIds.\n");
        sb.append("4. For each finding, suggest Intent Lock corrections: 'light' (minimal surgical change), 'balanced' (recommended standard), and 'strong' (formal corporate phrasing), preserving original author intent.\n\n");

        // Approved Sources
        sb.append("=== APPROVED SOURCES & GROUND-TRUTH EVIDENCE ===\n");
        if (request.getApprovedSources().isEmpty()) {
            sb.append("(No approved sources registered for this brand workspace)\n");
        } else {
            for (AiApprovedSourceSnippet src : request.getApprovedSources()) {
                sb.append("- Source ID: [").append(src.getSourceId()).append("] | Title: \"").append(src.getTitle()).append("\"\n");
                sb.append("  Doc Type: ").append(src.getSourceDocType() != null ? src.getSourceDocType() : "DOCUMENT").append("\n");
                if (src.getExcerpt() != null && !src.getExcerpt().isEmpty()) {
                    sb.append("  Excerpt: \"").append(src.getExcerpt()).append("\"\n");
                }
            }
        }
        sb.append("\n");

        // Approved Claims
        sb.append("=== APPROVED CLAIMS LEDGER ===\n");
        if (request.getApprovedClaims().isEmpty()) {
            sb.append("(No approved claims registered)\n");
        } else {
            for (AiApprovedClaimSnippet claim : request.getApprovedClaims()) {
                sb.append("- Claim ID: [").append(claim.getClaimId()).append("] | Claim: \"").append(claim.getClaimText()).append("\"\n");
                sb.append("  Status: ").append(claim.isExpired() ? "EXPIRED (Do not use)" : "ACTIVE").append(" | Scope: ").append(claim.getScope()).append("\n");
                if (claim.getSupportingSourceId() != null) {
                    sb.append("  Ground-truth source reference: ").append(claim.getSupportingSourceId()).append("\n");
                }
            }
        }
        sb.append("\n");

        // Brand Guidelines (Prohibited terms, required phrases, tone)
        sb.append("=== BRAND GUIDELINES & CONSTRAINTS ===\n");
        if (!request.getProhibitedExpressions().isEmpty()) {
            sb.append("Prohibited Expressions: ").append(String.join(", ", request.getProhibitedExpressions())).append("\n");
        }
        if (!request.getRequiredPhrases().isEmpty()) {
            sb.append("Required Disclaimers & Phrases: ").append(String.join("; ", request.getRequiredPhrases())).append("\n");
        }
        if (request.getToneGuidelines() != null && !request.getToneGuidelines().isEmpty()) {
            sb.append("Tone Guidelines: ").append(request.getToneGuidelines()).append("\n");
        }
        sb.append("\n");

        // Content to review
        sb.append("=== SUBMITTED CONTENT FOR REVIEW ===\n");
        sb.append(request.getContent()).append("\n\n");

        // Output format specification
        sb.append("=== REQUIRED JSON OUTPUT SCHEMA ===\n");
        sb.append("Respond ONLY with a valid JSON object matching this schema:\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"Brief 1-2 sentence overall governance summary\",\n");
        sb.append("  \"overallComplianceScore\": 85, // integer 0 to 100\n");
        sb.append("  \"findings\": [\n");
        sb.append("    {\n");
        sb.append("      \"category\": \"PROHIBITED_TERMINOLOGY | UNAPPROVED_CLAIM | EXPIRED_CLAIM | MISLEADING_CLAIM | BRAND_TONE | MISSING_DISCLAIMER | ACCESSIBILITY\",\n");
        sb.append("      \"severity\": \"CRITICAL | WARNING | ADVISORY\",\n");
        sb.append("      \"evidenceType\": \"VERIFIED_RULE | UNSUPPORTED_CLAIM | SUBJECTIVE_SUGGESTION\",\n");
        sb.append("      \"title\": \"Short finding title\",\n");
        sb.append("      \"contentExcerpt\": \"Exact verbatim text segment from the content that has the issue\",\n");
        sb.append("      \"explanation\": \"Clear explanation of the violation and why it matters\",\n");
        sb.append("      \"referencedSourceIds\": [\"source-id-1\"], // MUST only be IDs from APPROVED SOURCES, or empty\n");
        sb.append("      \"referencedClaimIds\": [\"claim-id-1\"], // MUST only be IDs from APPROVED CLAIMS, or empty\n");
        sb.append("      \"suggestedCorrections\": {\n");
        sb.append("        \"light\": \"Minimal surgical phrasing replacement\",\n");
        sb.append("        \"balanced\": \"Balanced standard phrasing replacement\",\n");
        sb.append("        \"strong\": \"Robust formal enterprise phrasing replacement\",\n");
        sb.append("        \"rationale\": \"Why this replacement preserves author intent while satisfying compliance\"\n");
        sb.append("      },\n");
        sb.append("      \"confidence\": 0.95 // numeric 0.0 to 1.0\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");

        return sb.toString();
    }

    /**
     * Translates Google Gen AI ApiException into application domain exceptions.
     */
    private void handleApiException(ApiException e, String model) {
        int code = e.code();
        log.error("Google Gemini API error (status: {}, code: {}): {}", e.status(), code, e.message());

        if (code == 401 || code == 403) {
            throw new AiAuthenticationException(
                    "Google Gemini authentication failed (HTTP " + code + "). Verify that your GOOGLE_API_KEY is valid and authorized.",
                    e
            );
        } else if (code == 404) {
            throw new AiModelNotFoundException(
                    "Configured Gemini model '" + model + "' was not found or is unavailable for the current API credentials.",
                    model,
                    e
            );
        } else if (code == 429) {
            throw new AiRateLimitException(
                    "Google Gemini API rate limit or quota exceeded (HTTP 429). Please retry after a brief pause.",
                    e
            );
        } else if (code >= 500) {
            throw new AiProviderException(
                    "Google Gemini service returned server error (HTTP " + code + "): " + e.status(),
                    e
            );
        } else {
            throw new AiProviderException(
                    "Google Gemini API error (HTTP " + code + "): " + e.message(),
                    e
            );
        }
    }

    /**
     * Parses the JSON returned by Gemini, enforces strict validation against hallucinated sources,
     * computes exact substring offsets within the original content, and sanitizes output.
     */
    protected AiReviewResult parseAndValidateResponse(String jsonText, AiReviewRequest request) {
        try {
            // Strip any accidental markdown formatting fences if present
            String cleanJson = jsonText.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JsonNode root = objectMapper.readTree(cleanJson);
            AiReviewResult result = new AiReviewResult();

            if (root.has("summary") && !root.get("summary").isNull()) {
                result.setSummary(root.get("summary").asText());
            } else {
                result.setSummary("Brand governance review completed by Gemini.");
            }

            if (root.has("overallComplianceScore") && root.get("overallComplianceScore").isNumber()) {
                result.setOverallComplianceScore(root.get("overallComplianceScore").asInt());
            } else {
                result.setOverallComplianceScore(80);
            }

            // Gather valid source and claim IDs for Zero-Hallucination verification
            Set<String> validSourceIds = request.getApprovedSources().stream()
                    .map(AiApprovedSourceSnippet::getSourceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<String> validClaimIds = request.getApprovedClaims().stream()
                    .map(AiApprovedClaimSnippet::getClaimId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            String fullContent = request.getContent();
            List<AiFindingDto> findings = new ArrayList<>();

            if (root.has("findings") && root.get("findings").isArray()) {
                for (JsonNode node : root.get("findings")) {
                    AiFindingDto finding = new AiFindingDto();

                    finding.setCategory(node.has("category") ? node.get("category").asText() : "BRAND_TONE");
                    finding.setSeverity(node.has("severity") ? node.get("severity").asText() : "WARNING");
                    finding.setEvidenceType(node.has("evidenceType") ? node.get("evidenceType").asText() : "VERIFIED_RULE");
                    finding.setTitle(node.has("title") ? node.get("title").asText() : "Brand Guideline Observation");
                    finding.setExplanation(node.has("explanation") ? node.get("explanation").asText() : "");

                    String excerpt = node.has("contentExcerpt") ? node.get("contentExcerpt").asText() : "";
                    finding.setContentExcerpt(excerpt);

                    // Compute precise substring offsets if the excerpt matches the original text
                    if (!excerpt.isEmpty() && fullContent.contains(excerpt)) {
                        int start = fullContent.indexOf(excerpt);
                        finding.setStartIndex(start);
                        finding.setEndIndex(start + excerpt.length());
                    } else {
                        finding.setStartIndex(-1);
                        finding.setEndIndex(-1);
                    }

                    // Zero-Hallucination Evidence Enforcement: Filter out any non-existent source IDs
                    List<String> validatedSourceIds = new ArrayList<>();
                    if (node.has("referencedSourceIds") && node.get("referencedSourceIds").isArray()) {
                        for (JsonNode srcNode : node.get("referencedSourceIds")) {
                            String srcId = srcNode.asText();
                            if (validSourceIds.contains(srcId)) {
                                validatedSourceIds.add(srcId);
                            } else {
                                log.warn("Zero-Hallucination Guard: Filtered invalid/hallucinated source ID '{}' from AI finding", srcId);
                            }
                        }
                    }
                    finding.setReferencedSourceIds(validatedSourceIds);

                    // Validate Claim IDs
                    List<String> validatedClaimIds = new ArrayList<>();
                    if (node.has("referencedClaimIds") && node.get("referencedClaimIds").isArray()) {
                        for (JsonNode clmNode : node.get("referencedClaimIds")) {
                            String clmId = clmNode.asText();
                            if (validClaimIds.contains(clmId)) {
                                validatedClaimIds.add(clmId);
                            } else {
                                log.warn("Zero-Hallucination Guard: Filtered invalid claim ID '{}' from AI finding", clmId);
                            }
                        }
                    }
                    finding.setReferencedClaimIds(validatedClaimIds);

                    // Suggested Corrections (Intent Lock variants)
                    if (node.has("suggestedCorrections") && node.get("suggestedCorrections").isObject()) {
                        JsonNode corrNode = node.get("suggestedCorrections");
                        AiSuggestedCorrectionDto corr = new AiSuggestedCorrectionDto();
                        corr.setLight(corrNode.has("light") ? corrNode.get("light").asText() : excerpt);
                        corr.setBalanced(corrNode.has("balanced") ? corrNode.get("balanced").asText() : excerpt);
                        corr.setStrong(corrNode.has("strong") ? corrNode.get("strong").asText() : excerpt);
                        corr.setRationale(corrNode.has("rationale") ? corrNode.get("rationale").asText() : "Aligned with brand standards.");
                        corr.setPreservesIntent(true);
                        finding.setSuggestedCorrections(corr);
                    }

                    // Confidence score
                    if (node.has("confidence") && node.get("confidence").isNumber()) {
                        finding.setConfidence(node.get("confidence").asDouble());
                    } else {
                        finding.setConfidence(1.0);
                    }

                    findings.add(finding);
                }
            }

            result.setFindings(findings);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini structured JSON response: {}", e.getMessage());
            throw new AiProviderException("Malformed JSON received from Gemini: " + e.getMessage(), e);
        }
    }
}
