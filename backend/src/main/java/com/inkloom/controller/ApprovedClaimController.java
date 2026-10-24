package com.inkloom.controller;

import com.inkloom.dto.request.ApprovedClaimRequest;
import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.ApprovedClaimResponse;
import com.inkloom.service.ApprovedClaimService;
import com.inkloom.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands/{brandId}/claims")
public class ApprovedClaimController {

    private final ApprovedClaimService claimService;

    public ApprovedClaimController(ApprovedClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping
    @PreAuthorize("@workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<List<ApprovedClaimResponse>>> getClaims(@PathVariable String brandId) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getClaimsByBrand(brandId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<ApprovedClaimResponse>> createClaim(
            @PathVariable String brandId,
            @Valid @RequestBody ApprovedClaimRequest request) {
        ApprovedClaimResponse response = claimService.createClaim(brandId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Approved claim registered", response));
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("@workspaceSecurity.canAccessClaim(#claimId)")
    public ResponseEntity<ApiResponse<ApprovedClaimResponse>> getClaimById(
            @PathVariable String brandId,
            @PathVariable String claimId) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getClaimById(claimId)));
    }

    @PutMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.canAccessClaim(#claimId)")
    public ResponseEntity<ApiResponse<ApprovedClaimResponse>> updateClaim(
            @PathVariable String brandId,
            @PathVariable String claimId,
            @Valid @RequestBody ApprovedClaimRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Approved claim updated", claimService.updateClaim(brandId, claimId, request)));
    }

    @DeleteMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.canAccessClaim(#claimId)")
    public ResponseEntity<ApiResponse<Void>> deleteClaim(
            @PathVariable String brandId,
            @PathVariable String claimId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = (principal != null) ? principal.getId() : null;
        claimService.deleteClaim(brandId, claimId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Approved claim deleted successfully", null));
    }

    @PatchMapping("/{claimId}/status")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.canAccessClaim(#claimId)")
    public ResponseEntity<ApiResponse<ApprovedClaimResponse>> updateClaimStatus(
            @PathVariable String brandId,
            @PathVariable String claimId,
            @RequestParam String status,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = (principal != null) ? principal.getId() : null;
        ApprovedClaimResponse response = claimService.updateClaimStatus(brandId, claimId, status, userId);
        return ResponseEntity.ok(ApiResponse.ok("Claim status updated", response));
    }
}
