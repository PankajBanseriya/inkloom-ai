package com.inkloom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkloom.config.CorsConfig;
import com.inkloom.config.SecurityConfig;
import com.inkloom.dto.request.ApprovedClaimRequest;
import com.inkloom.dto.response.ApprovedClaimResponse;
import com.inkloom.model.ApprovedClaim;
import com.inkloom.service.ApprovedClaimService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApprovedClaimController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class ApprovedClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovedClaimService claimService;

    @Test
    @DisplayName("GET /api/v1/brands/{brandId}/claims returns list of claims")
    void testGetClaims() throws Exception {
        ApprovedClaim claim = new ApprovedClaim("brand-1", "99.99% SLA verified", "SOC2 Report", "Global", "Infra Lead", Instant.now().plusSeconds(864000));
        claim.setId("claim-1");

        when(claimService.getClaimsByBrand("brand-1")).thenReturn(List.of(new ApprovedClaimResponse(claim)));

        mockMvc.perform(get("/api/v1/brands/brand-1/claims")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].claimText").value("99.99% SLA verified"));
    }

    @Test
    @DisplayName("POST /api/v1/brands/{brandId}/claims creates new claim")
    void testCreateClaim() throws Exception {
        ApprovedClaimRequest request = new ApprovedClaimRequest("99.99% SLA verified", "SOC2 Report", "Global", "Infra Lead", Instant.now().plusSeconds(864000));
        ApprovedClaim claim = new ApprovedClaim("brand-1", request.getClaimText(), request.getSupportingSource(), request.getScope(), request.getOwner(), request.getExpiryDate());
        claim.setId("claim-1");

        when(claimService.createClaim(eq("brand-1"), any(ApprovedClaimRequest.class))).thenReturn(new ApprovedClaimResponse(claim));

        mockMvc.perform(post("/api/v1/brands/brand-1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("claim-1"));
    }
}
