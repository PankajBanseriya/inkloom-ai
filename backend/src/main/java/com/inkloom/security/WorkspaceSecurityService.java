package com.inkloom.security;

import com.inkloom.model.ApprovedClaim;
import com.inkloom.model.ApprovedSource;
import com.inkloom.model.Review;
import com.inkloom.model.Revision;
import com.inkloom.model.Role;
import com.inkloom.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("workspaceSecurity")
public class WorkspaceSecurityService {

    private final BrandWorkspaceRepository workspaceRepository;
    private final ReviewRepository reviewRepository;
    private final ApprovedClaimRepository claimRepository;
    private final ApprovedSourceRepository sourceRepository;
    private final RevisionRepository revisionRepository;

    public WorkspaceSecurityService(BrandWorkspaceRepository workspaceRepository,
                                  ReviewRepository reviewRepository,
                                  ApprovedClaimRepository claimRepository,
                                  ApprovedSourceRepository sourceRepository,
                                  RevisionRepository revisionRepository) {
        this.workspaceRepository = workspaceRepository;
        this.reviewRepository = reviewRepository;
        this.claimRepository = claimRepository;
        this.sourceRepository = sourceRepository;
        this.revisionRepository = revisionRepository;
    }

    public UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return null;
    }

    public boolean hasWorkspaceAccess(String brandId) {
        if (brandId == null || brandId.isBlank()) {
            return false;
        }

        UserPrincipal user = getCurrentUser();
        if (user == null) {
            return false;
        }

        // Global admin has oversight access to all workspaces
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Check if workspace is in user's explicit workspace assignments
        if (user.getWorkspaceIds() != null && user.getWorkspaceIds().contains(brandId)) {
            return true;
        }

        // Check if user is the creator/owner or member of the workspace
        return workspaceRepository.findById(brandId)
                .map(workspace -> user.getId().equals(workspace.getOwnerUserId())
                        || (workspace.getMemberUserIds() != null && workspace.getMemberUserIds().contains(user.getId())))
                .orElse(false);
    }

    public boolean canAccessReview(String reviewId) {
        if (reviewId == null || reviewId.isBlank()) {
            return false;
        }

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return true; // Let controller handle 404
        }

        return hasWorkspaceAccess(review.getBrandId());
    }

    public boolean canAccessClaim(String claimId) {
        if (claimId == null || claimId.isBlank()) {
            return false;
        }

        ApprovedClaim claim = claimRepository.findById(claimId).orElse(null);
        if (claim == null) {
            return true; // Let controller handle 404
        }

        return hasWorkspaceAccess(claim.getBrandId());
    }

    public boolean canAccessSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return false;
        }

        ApprovedSource source = sourceRepository.findById(sourceId).orElse(null);
        if (source == null) {
            return true; // Let controller handle 404
        }

        return hasWorkspaceAccess(source.getBrandId());
    }

    public boolean canAccessRevision(String revisionId) {
        if (revisionId == null || revisionId.isBlank()) {
            return false;
        }

        Revision rev = revisionRepository.findById(revisionId).orElse(null);
        if (rev == null) {
            return true;
        }

        return canAccessReview(rev.getReviewId());
    }
}
