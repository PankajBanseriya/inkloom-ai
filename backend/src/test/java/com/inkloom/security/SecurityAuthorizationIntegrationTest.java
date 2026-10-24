package com.inkloom.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkloom.controller.ApprovalController;
import com.inkloom.controller.BrandController;
import com.inkloom.dto.request.ReviewDecisionRequest;
import com.inkloom.dto.response.ReviewDecisionResponse;
import com.inkloom.model.BrandGuideline;
import com.inkloom.model.ReviewDecision;
import com.inkloom.service.ApprovalService;
import com.inkloom.service.BrandWorkspaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({BrandController.class, ApprovalController.class})
@Import(com.inkloom.config.TestSecurityConfig.class)
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BrandWorkspaceService brandWorkspaceService;

    @MockBean
    private ApprovalService approvalService;

    @Autowired
    private WorkspaceSecurityService workspaceSecurity;

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401 Unauthorized")
    void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/brands/brand-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(username = "writer-1", roles = {"CONTENT_CREATOR"})
    @DisplayName("CONTENT_CREATOR is forbidden from modifying brand guidelines (403 Forbidden)")
    void testContentCreatorCannotModifyGuidelines() throws Exception {
        when(workspaceSecurity.hasWorkspaceAccess("brand-10")).thenReturn(true);

        BrandGuideline guideline = new BrandGuideline("brand-10", "v2.0", "writer-1");

        mockMvc.perform(put("/api/v1/brands/brand-10/guidelines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guideline)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "writer-1", roles = {"CONTENT_CREATOR"})
    @DisplayName("CONTENT_CREATOR is forbidden from making review decisions (403 Forbidden)")
    void testContentCreatorCannotMakeReviewDecision() throws Exception {
        when(workspaceSecurity.canAccessReview("rev-10")).thenReturn(true);

        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "Attempting self signoff");

        mockMvc.perform(post("/api/v1/reviews/rev-10/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "external-user", roles = {"BRAND_MANAGER"})
    @DisplayName("User accessing unauthorized brand workspace is blocked (403 Forbidden workspace isolation)")
    void testWorkspaceIsolationEnforced() throws Exception {
        // WorkspaceSecurity rejects access because user doesn't belong to this workspace
        when(workspaceSecurity.hasWorkspaceAccess("brand-restricted")).thenReturn(false);

        mockMvc.perform(get("/api/v1/brands/brand-restricted")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "approver-1", roles = {"REVIEWER"})
    @DisplayName("REVIEWER with workspace access successfully records decision (200 OK)")
    void testReviewerAuthorizedDecision() throws Exception {
        when(workspaceSecurity.canAccessReview("rev-10")).thenReturn(true);

        ReviewDecision decision = new ReviewDecision("rev-10", "approver-1", "Sarah Legal", "APPROVE", "Compliant");
        decision.setId("dec-1");
        when(approvalService.makeDecision(eq("rev-10"), any(ReviewDecisionRequest.class), any(), any()))
                .thenReturn(new ReviewDecisionResponse(decision));

        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "Compliant");

        mockMvc.perform(post("/api/v1/reviews/rev-10/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.action").value("APPROVE"));
    }
}
