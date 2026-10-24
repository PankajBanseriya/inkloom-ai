package com.inkloom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkloom.dto.request.ReviewCommentRequest;
import com.inkloom.dto.request.ReviewDecisionRequest;
import com.inkloom.dto.response.ReviewCommentResponse;
import com.inkloom.dto.response.ReviewDecisionResponse;
import com.inkloom.model.ReviewComment;
import com.inkloom.model.ReviewDecision;
import com.inkloom.security.WorkspaceSecurityService;
import com.inkloom.service.ApprovalService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApprovalController.class)
@Import(com.inkloom.config.TestSecurityConfig.class)
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovalService approvalService;

    @Autowired
    private WorkspaceSecurityService workspaceSecurity;

    @BeforeEach
    void setUp() {
        when(workspaceSecurity.canAccessReview(any())).thenReturn(true);
    }

    @Test
    @WithMockUser(username = "reviewer-1", roles = {"REVIEWER"})
    @DisplayName("POST /api/v1/reviews/{reviewId}/comments adds a comment")
    void testAddComment() throws Exception {
        ReviewCommentRequest request = new ReviewCommentRequest("Clarify benchmark source");
        ReviewComment comment = new ReviewComment("rev-1", "user-1", "Elena Brand", "BRAND_MANAGER", request.getCommentText());
        comment.setId("c-1");

        when(approvalService.addComment(eq("rev-1"), any(ReviewCommentRequest.class), any(), any(), any()))
                .thenReturn(new ReviewCommentResponse(comment));

        mockMvc.perform(post("/api/v1/reviews/rev-1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentText").value("Clarify benchmark source"));
    }

    @Test
    @WithMockUser(username = "reviewer-1", roles = {"REVIEWER"})
    @DisplayName("POST /api/v1/reviews/{reviewId}/decisions records review decision")
    void testMakeDecision() throws Exception {
        ReviewDecisionRequest request = new ReviewDecisionRequest("APPROVE", "Signed off by Legal");
        ReviewDecision decision = new ReviewDecision("rev-1", "user-2", "Dan Legal", "APPROVE", "Signed off by Legal");
        decision.setId("dec-1");

        when(approvalService.makeDecision(eq("rev-1"), any(ReviewDecisionRequest.class), any(), any()))
                .thenReturn(new ReviewDecisionResponse(decision));

        mockMvc.perform(post("/api/v1/reviews/rev-1/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.action").value("APPROVE"));
    }
}
