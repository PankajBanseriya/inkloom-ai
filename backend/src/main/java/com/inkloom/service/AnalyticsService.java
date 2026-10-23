package com.inkloom.service;

import com.inkloom.dto.response.WorkspaceAnalyticsResponse;
import com.inkloom.exception.ResourceNotFoundException;
import com.inkloom.model.Finding;
import com.inkloom.model.Review;
import com.inkloom.repository.BrandWorkspaceRepository;
import com.inkloom.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ReviewRepository reviewRepository;
    private final BrandWorkspaceRepository workspaceRepository;

    public AnalyticsService(ReviewRepository reviewRepository, BrandWorkspaceRepository workspaceRepository) {
        this.reviewRepository = reviewRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public WorkspaceAnalyticsResponse getWorkspaceAnalytics(String brandId) {
        if (!workspaceRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("BrandWorkspace", "id", brandId);
        }

        List<Review> reviews = reviewRepository.findByBrandIdOrderByCreatedAtDesc(brandId);
        long totalReviews = reviews.size();
        long pendingReviews = reviews.stream().filter(r -> "IN_REVIEW".equalsIgnoreCase(r.getStatus())).count();
        long approvedReviews = reviews.stream().filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus())).count();
        long changesRequested = reviews.stream().filter(r -> "CHANGES_REQUESTED".equalsIgnoreCase(r.getStatus())).count();

        double avgScore = reviews.isEmpty() ? 100.0 :
                Math.round(reviews.stream().mapToDouble(Review::getComplianceScore).average().orElse(100.0) * 10.0) / 10.0;

        long issuesIdentified = 0;
        Map<String, Long> categoryBreakdown = new HashMap<>();

        for (Review r : reviews) {
            if (r.getFindings() != null) {
                issuesIdentified += r.getFindings().size();
                for (Finding f : r.getFindings()) {
                    String cat = f.getCategory() != null ? f.getCategory() : "OTHER";
                    categoryBreakdown.put(cat, categoryBreakdown.getOrDefault(cat, 0L) + 1L);
                }
            }
        }

        return new WorkspaceAnalyticsResponse(
                brandId,
                totalReviews,
                pendingReviews,
                approvedReviews,
                changesRequested,
                issuesIdentified,
                avgScore,
                categoryBreakdown
        );
    }
}
