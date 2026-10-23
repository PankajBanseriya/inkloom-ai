package com.inkloom.controller;

import com.inkloom.dto.request.ReviewCommentRequest;
import com.inkloom.dto.request.ReviewDecisionRequest;
import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.ReviewCommentResponse;
import com.inkloom.dto.response.ReviewDecisionResponse;
import com.inkloom.service.ApprovalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<ReviewCommentResponse>> addComment(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewCommentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        ReviewCommentResponse response = approvalService.addComment(reviewId, request, userId, userName, userRole);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Comment added successfully", response));
    }

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<List<ReviewCommentResponse>>> getComments(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(approvalService.getComments(reviewId)));
    }

    @PostMapping("/decisions")
    public ResponseEntity<ApiResponse<ReviewDecisionResponse>> makeDecision(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewDecisionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        ReviewDecisionResponse response = approvalService.makeDecision(reviewId, request, userId, userName);
        return ResponseEntity.ok(ApiResponse.ok("Review decision recorded successfully", response));
    }

    @GetMapping("/decisions")
    public ResponseEntity<ApiResponse<List<ReviewDecisionResponse>>> getDecisions(@PathVariable String reviewId) {
        return ResponseEntity.ok(ApiResponse.ok(approvalService.getDecisions(reviewId)));
    }
}
