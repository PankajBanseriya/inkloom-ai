package com.inkloom.controller;

import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.WorkspaceAnalyticsResponse;
import com.inkloom.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/brands/{brandId}")
    @PreAuthorize("@workspaceSecurity.hasWorkspaceAccess(#brandId)")
    public ResponseEntity<ApiResponse<WorkspaceAnalyticsResponse>> getWorkspaceAnalytics(@PathVariable String brandId) {
        WorkspaceAnalyticsResponse response = analyticsService.getWorkspaceAnalytics(brandId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
