package com.inkloom.controller;

import com.inkloom.dto.request.ApprovedSourceRequest;
import com.inkloom.dto.request.BrandWorkspaceRequest;
import com.inkloom.dto.request.InviteMemberRequest;
import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.ApprovedSourceResponse;
import com.inkloom.dto.response.BrandGuidelineResponse;
import com.inkloom.dto.response.BrandWorkspaceResponse;
import com.inkloom.dto.response.UserResponse;
import com.inkloom.dto.response.WorkspaceAnalyticsResponse;
import com.inkloom.model.BrandGuideline;
import com.inkloom.security.UserPrincipal;
import com.inkloom.service.AnalyticsService;
import com.inkloom.service.BrandWorkspaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandWorkspaceService workspaceService;
    private AnalyticsService analyticsService;

    public BrandController(BrandWorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Autowired(required = false)
    public void setAnalyticsService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN', 'CONTENT_CREATOR')")
    public ResponseEntity<ApiResponse<BrandWorkspaceResponse>> createWorkspace(
            @Valid @RequestBody BrandWorkspaceRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String effectiveUserId = (principal != null) ? principal.getId() : userId;
        BrandWorkspaceResponse response = workspaceService.createWorkspace(request, effectiveUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Brand workspace created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandWorkspaceResponse>>> getAllWorkspaces(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(workspaceService.getWorkspacesForUser(principal)));
    }

    @GetMapping("/{brandId}")
    @PreAuthorize("@workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<BrandWorkspaceResponse>> getWorkspaceById(@PathVariable String brandId) {
        return ResponseEntity.ok(ApiResponse.ok(workspaceService.getWorkspaceById(brandId)));
    }

    @PutMapping("/{brandId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<BrandWorkspaceResponse>> updateWorkspace(
            @PathVariable String brandId,
            @Valid @RequestBody BrandWorkspaceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Workspace updated", workspaceService.updateWorkspace(brandId, request)));
    }

    @GetMapping("/{brandId}/guidelines")
    @PreAuthorize("@workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<BrandGuidelineResponse>> getGuidelines(@PathVariable String brandId) {
        return ResponseEntity.ok(ApiResponse.ok(workspaceService.getGuideline(brandId)));
    }

    @PutMapping("/{brandId}/guidelines")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<BrandGuidelineResponse>> updateGuidelines(
            @PathVariable String brandId,
            @RequestBody BrandGuideline guideline,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String effectiveUserId = (principal != null) ? principal.getId() : userId;
        return ResponseEntity.ok(ApiResponse.ok("Guidelines updated", workspaceService.updateGuideline(brandId, guideline, effectiveUserId)));
    }

    @GetMapping("/{brandId}/sources")
    @PreAuthorize("@workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<List<ApprovedSourceResponse>>> getSources(@PathVariable String brandId) {
        return ResponseEntity.ok(ApiResponse.ok(workspaceService.getSources(brandId)));
    }

    @PostMapping("/{brandId}/sources")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<ApprovedSourceResponse>> createSource(
            @PathVariable String brandId,
            @Valid @RequestBody ApprovedSourceRequest request) {
        ApprovedSourceResponse response = workspaceService.createSource(brandId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Approved source registered", response));
    }

    @PutMapping("/{brandId}/sources/{sourceId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<ApprovedSourceResponse>> updateSource(
            @PathVariable String brandId,
            @PathVariable String sourceId,
            @Valid @RequestBody ApprovedSourceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Approved source updated", workspaceService.updateSource(brandId, sourceId, request)));
    }

    @DeleteMapping("/{brandId}/sources/{sourceId}")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<Void>> deleteSource(
            @PathVariable String brandId,
            @PathVariable String sourceId) {
        workspaceService.deleteSource(brandId, sourceId);
        return ResponseEntity.ok(ApiResponse.ok("Approved source deleted successfully", null));
    }

    @GetMapping("/{brandId}/members")
    @PreAuthorize("@workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getMembers(@PathVariable String brandId) {
        return ResponseEntity.ok(ApiResponse.ok(workspaceService.getWorkspaceMembers(brandId)));
    }

    @PostMapping("/{brandId}/members")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN') and @workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<UserResponse>> inviteMember(
            @PathVariable String brandId,
            @Valid @RequestBody InviteMemberRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String effectiveUserId = (principal != null) ? principal.getId() : userId;
        UserResponse response = workspaceService.inviteMember(brandId, request, effectiveUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Team member invited successfully", response));
    }

    @GetMapping("/{brandId}/analytics")
    @PreAuthorize("@workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<WorkspaceAnalyticsResponse>> getWorkspaceAnalytics(@PathVariable String brandId) {
        if (analyticsService == null) {
            throw new IllegalStateException("Analytics service not configured");
        }
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getWorkspaceAnalytics(brandId)));
    }
}
