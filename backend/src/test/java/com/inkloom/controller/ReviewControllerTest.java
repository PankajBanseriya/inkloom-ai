package com.inkloom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkloom.config.CorsConfig;
import com.inkloom.config.SecurityConfig;
import com.inkloom.dto.request.CorrectionAcceptRequest;
import com.inkloom.dto.request.ReviewSubmissionRequest;
import com.inkloom.dto.response.ReviewResponse;
import com.inkloom.model.Finding;
import com.inkloom.model.Review;
import com.inkloom.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @Test
    @DisplayName("POST /api/v1/reviews returns 201 Created and evaluation payload")
    void testSubmitReview() throws Exception {
        ReviewSubmissionRequest request = new ReviewSubmissionRequest(
                "brand-123",
                "Product Announcement",
                "MARKETING_COPY",
                "Our software guarantees zero downtime."
        );

        Review review = new Review("brand-123", "author-1", "Product Announcement", "MARKETING_COPY", request.getContent());
        review.setId("rev-101");
        review.setStatus("IN_REVIEW");
        review.setComplianceScore(92.0);

        when(reviewService.submitReview(any(ReviewSubmissionRequest.class), any())).thenReturn(new ReviewResponse(review));

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("rev-101"))
                .andExpect(jsonPath("$.data.complianceScore").value(92.0));
    }

    @Test
    @DisplayName("GET /api/v1/reviews/{reviewId} returns 200 OK")
    void testGetReviewById() throws Exception {
        Review review = new Review("brand-123", "author-1", "Product Announcement", "MARKETING_COPY", "Text");
        review.setId("rev-101");
        review.setStatus("IN_REVIEW");

        when(reviewService.getReviewById("rev-101")).thenReturn(new ReviewResponse(review));

        mockMvc.perform(get("/api/v1/reviews/rev-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Product Announcement"));
    }

    @Test
    @DisplayName("POST /api/v1/reviews/{reviewId}/accept-correction applies Intent Lock")
    void testAcceptCorrection() throws Exception {
        CorrectionAcceptRequest request = new CorrectionAcceptRequest(
                "find-1",
                "BALANCED",
                "delivers industry-leading reliability",
                "Replaced absolute claim"
        );

        Review review = new Review("brand-123", "author-1", "Product Announcement", "MARKETING_COPY", "Text");
        review.setId("rev-101");
        review.setCurrentContent("Text with replacement");
        review.setActiveRevisionNumber(2);

        when(reviewService.acceptCorrection(eq("rev-101"), any(CorrectionAcceptRequest.class), any()))
                .thenReturn(new ReviewResponse(review));

        mockMvc.perform(post("/api/v1/reviews/rev-101/accept-correction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeRevisionNumber").value(2));
    }
}
