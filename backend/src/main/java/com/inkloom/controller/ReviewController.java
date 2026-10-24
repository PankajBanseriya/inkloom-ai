package com.inkloom.controller;

import com.inkloom.dto.request.CorrectionAcceptRequest;
import com.inkloom.dto.request.ReviewSubmissionRequest;
import com.inkloom.dto.request.RevisionCreateRequest;
import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.ReviewResponse;
import com.inkloom.dto.response.RevisionResponse;
import com.inkloom.model.Finding;
import com.inkloom.model.SuggestedCorrection;
import com.inkloom.security.UserPrincipal;
import com.inkloom.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CONTENT_CREATOR', 'BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.hasWorkspaceAccess(#request.brandId)")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @Valid @RequestBody ReviewSubmissionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String effectiveUserId = (principal != null) ? principal.getId() : userId;
        ReviewResponse response = reviewService.submitReview(request, effectiveUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review submitted and evaluated successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @RequestParam(value = "brandId", required = false) String brandId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (brandId != null && !brandId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(reviewService.getReviewsByBrand(brandId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getAllReviews()));
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("@workspaceSecurity.canAccessReview(#reviewId)")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getReviewById(reviewId)));
    }

    @GetMapping("/{reviewId}/findings")
    @PreAuthorize("@workspaceSecurity.canAccessReview(#reviewId)")
    public ResponseEntity<ApiResponse<List<Finding>>> getFindings(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getFindings(reviewId)));
    }

    @GetMapping("/{reviewId}/suggestions")
    @PreAuthorize("@workspaceSecurity.canAccessReview(#reviewId)")
    public ResponseEntity<ApiResponse<List<SuggestedCorrection>>> getSuggestions(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getSuggestions(reviewId)));
    }

    @PostMapping(value = {"/{reviewId}/accept-correction", "/{reviewId}/corrections/accept"})
    @PreAuthorize("hasAnyRole('CONTENT_CREATOR', 'BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.canAccessReview(#reviewId)")
    public ResponseEntity<ApiResponse<ReviewResponse>> acceptCorrection(
            @PathVariable String reviewId,
            @Valid @RequestBody CorrectionAcceptRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String effectiveUserId = (principal != null) ? principal.getId() : userId;
        ReviewResponse response = reviewService.acceptCorrection(reviewId, request, effectiveUserId);
        return ResponseEntity.ok(ApiResponse.ok("Correction accepted and non-destructive revision created", response));
    }

    @PostMapping(value = {"/{reviewId}/reject-correction", "/{reviewId}/corrections/reject"})
    @PreAuthorize("@workspaceSecurity.canAccessReview(#reviewId)")
    public ResponseEntity<ApiResponse<ReviewResponse>> rejectCorrection(
            @PathVariable String reviewId,
            @RequestParam String findingId,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String effectiveUserId = (principal != null) ? principal.getId() : (userId != null ? userId : "user-default");
        ReviewResponse response = reviewService.rejectCorrection(reviewId, findingId, reason, effectiveUserId);
        return ResponseEntity.ok(ApiResponse.ok("Correction rejected; original text preserved", response));
    }

    @PostMapping("/{reviewId}/revisions")
    @PreAuthorize("hasAnyRole('CONTENT_CREATOR', 'BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.canAccessReview(#reviewId)")
    public ResponseEntity<ApiResponse<RevisionResponse>> saveRevision(
            @PathVariable String reviewId,
            @Valid @RequestBody RevisionCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String effectiveUserId = (principal != null) ? principal.getId() : userId;
        RevisionResponse response = reviewService.saveRevision(reviewId, request, effectiveUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("New revision saved successfully", response));
    }

    @GetMapping(value = {"/{reviewId}/history", "/{reviewId}/revisions"})
    @PreAuthorize("@workspaceSecurity.canAccessReview(#reviewId)")
    public ResponseEntity<ApiResponse<List<RevisionResponse>>> getRevisionHistory(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getRevisionHistory(reviewId)));
    }
}
