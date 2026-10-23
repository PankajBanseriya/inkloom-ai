package com.inkloom.controller;

import com.inkloom.config.CorsConfig;
import com.inkloom.config.SecurityConfig;
import com.inkloom.dto.response.WorkspaceAnalyticsResponse;
import com.inkloom.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("GET /api/v1/analytics/brands/{brandId} returns metrics")
    void testGetWorkspaceAnalytics() throws Exception {
        WorkspaceAnalyticsResponse response = new WorkspaceAnalyticsResponse(
                "brand-101",
                45L,
                12L,
                28L,
                5L,
                110L,
                94.6,
                Map.of("PROHIBITED_TERMINOLOGY", 40L, "UNSUPPORTED_CLAIM", 30L)
        );

        when(analyticsService.getWorkspaceAnalytics("brand-101")).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/brands/brand-101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalReviews").value(45))
                .andExpect(jsonPath("$.data.avgComplianceScore").value(94.6))
                .andExpect(jsonPath("$.data.categoryBreakdown.PROHIBITED_TERMINOLOGY").value(40));
    }
}
