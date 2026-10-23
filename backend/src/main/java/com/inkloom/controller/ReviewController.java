package com.inkloom.controller;

import com.inkloom.dto.request.CorrectionAcceptRequest;
import com.inkloom.dto.request.ReviewSubmissionRequest;
import com.inkloom.dto.request.RevisionCreateRequest;
import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.ReviewResponse;
import com.inkloom.dto.response.RevisionResponse;
import com.inkloom.model.Finding;
import com.inkloom.model.SuggestedCorrection;
import com.inkloom.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @Valid @RequestBody ReviewSubmissionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ReviewResponse response = reviewService.submitReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review submitted and evaluated successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @RequestParam(value = "brandId", required = false) String brandId) {
        List<ReviewResponse> reviews = (brandId != null && !brandId.isBlank())
                ? reviewService.getReviewsByBrand(brandId)
                : reviewService.getAllReviews();
        return ResponseEntity.ok(ApiResponse.ok(reviews));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getReviewById(reviewId)));
    }

    @GetMapping("/{reviewId}/findings")
    public ResponseEntity<ApiResponse<List<Finding>>> getFindings(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getFindings(reviewId)));
    }

    @GetMapping("/{reviewId}/suggestions")
    public ResponseEntity<ApiResponse<List<SuggestedCorrection>>> getSuggestions(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getSuggestions(reviewId)));
    }

    @PostMapping("/{reviewId}/accept-correction")
    public ResponseEntity<ApiResponse<ReviewResponse>> acceptCorrection(
            @PathVariable String reviewId,
            @Valid @RequestBody CorrectionAcceptRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ReviewResponse response = reviewService.acceptCorrection(reviewId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok("Correction accepted and non-destructive revision created", response));
    }

    @PostMapping("/{reviewId}/revisions")
    public ResponseEntity<ApiResponse<RevisionResponse>> saveRevision(
            @PathVariable String reviewId,
            @Valid @RequestBody RevisionCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        RevisionResponse response = reviewService.saveRevision(reviewId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("New revision saved successfully", response));
    }

    @GetMapping("/{reviewId}/history")
    public ResponseEntity<ApiResponse<List<RevisionResponse>>> getRevisionHistory(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getRevisionHistory(reviewId)));
    }
}
