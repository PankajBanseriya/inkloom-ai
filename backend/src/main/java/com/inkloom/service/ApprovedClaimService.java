package com.inkloom.service;

import com.inkloom.dto.request.ApprovedClaimRequest;
import com.inkloom.dto.response.ApprovedClaimResponse;
import com.inkloom.exception.ResourceNotFoundException;
import com.inkloom.model.ApprovedClaim;
import com.inkloom.model.AuditEvent;
import com.inkloom.repository.ApprovedClaimRepository;
import com.inkloom.repository.AuditEventRepository;
import com.inkloom.repository.BrandWorkspaceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovedClaimService {

    private final ApprovedClaimRepository claimRepository;
    private final BrandWorkspaceRepository workspaceRepository;
    private final AuditEventRepository auditEventRepository;

    public ApprovedClaimService(ApprovedClaimRepository claimRepository,
                                BrandWorkspaceRepository workspaceRepository,
                                AuditEventRepository auditEventRepository) {
        this.claimRepository = claimRepository;
        this.workspaceRepository = workspaceRepository;
        this.auditEventRepository = auditEventRepository;
    }

    public List<ApprovedClaimResponse> getClaimsByBrand(String brandId) {
        return claimRepository.findByBrandId(brandId).stream()
                .map(ApprovedClaimResponse::new)
                .collect(Collectors.toList());
    }

    public ApprovedClaimResponse getClaimById(String claimId) {
        ApprovedClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovedClaim", "id", claimId));
        return new ApprovedClaimResponse(claim);
    }

    public ApprovedClaimResponse createClaim(String brandId, ApprovedClaimRequest request) {
        if (!workspaceRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("BrandWorkspace", "id", brandId);
        }

        ApprovedClaim claim = new ApprovedClaim(
                brandId,
                request.getClaimText(),
                request.getSupportingSource(),
                request.getScope(),
                request.getOwner(),
                request.getExpiryDate()
        );
        claim.setSupportingSourceId(request.getSupportingSourceId());

        ApprovedClaim saved = claimRepository.save(claim);

        auditEventRepository.save(new AuditEvent(
                brandId,
                null,
                "CLAIM_CREATED",
                "ApprovedClaim",
                saved.getId(),
                "Registered claim: " + saved.getClaimText()
        ));

        return new ApprovedClaimResponse(saved);
    }

    public ApprovedClaimResponse updateClaim(String brandId, String claimId, ApprovedClaimRequest request) {
        ApprovedClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovedClaim", "id", claimId));

        claim.setClaimText(request.getClaimText());
        claim.setSupportingSource(request.getSupportingSource());
        claim.setSupportingSourceId(request.getSupportingSourceId());
        claim.setScope(request.getScope());
        claim.setOwner(request.getOwner());
        if (request.getExpiryDate() != null) {
            claim.setExpiryDate(request.getExpiryDate());
        }
        claim.setUpdatedAt(Instant.now());
        ApprovedClaim saved = claimRepository.save(claim);

        auditEventRepository.save(new AuditEvent(
                brandId,
                null,
                "CLAIM_UPDATED",
                "ApprovedClaim",
                saved.getId(),
                "Updated claim: " + saved.getClaimText()
        ));

        return new ApprovedClaimResponse(saved);
    }

    public ApprovedClaimResponse updateClaimStatus(String brandId, String claimId, String status, String userId) {
        ApprovedClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovedClaim", "id", claimId));

        String oldStatus = claim.getStatus();
        claim.setStatus(status.toUpperCase().trim());
        claim.setUpdatedAt(Instant.now());
        ApprovedClaim saved = claimRepository.save(claim);

        auditEventRepository.save(new AuditEvent(
                brandId,
                userId,
                "CLAIM_STATUS_UPDATED",
                "ApprovedClaim",
                saved.getId(),
                "Status changed from " + oldStatus + " to " + saved.getStatus()
        ));

        return new ApprovedClaimResponse(saved);
    }

    public void deleteClaim(String brandId, String claimId, String userId) {
        ApprovedClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovedClaim", "id", claimId));

        claimRepository.delete(claim);

        auditEventRepository.save(new AuditEvent(
                brandId,
                userId,
                "CLAIM_DELETED",
                "ApprovedClaim",
                claimId,
                "Deleted claim: " + claim.getClaimText()
        ));
    }
}
