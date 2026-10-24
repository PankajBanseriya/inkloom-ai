package com.inkloom.service;

import com.inkloom.dto.request.ApprovedSourceRequest;
import com.inkloom.dto.request.BrandWorkspaceRequest;
import com.inkloom.dto.response.ApprovedSourceResponse;
import com.inkloom.dto.response.BrandGuidelineResponse;
import com.inkloom.dto.response.BrandWorkspaceResponse;
import com.inkloom.exception.ResourceNotFoundException;
import com.inkloom.model.ApprovedSource;
import com.inkloom.model.AuditEvent;
import com.inkloom.model.BrandGuideline;
import com.inkloom.model.BrandWorkspace;
import com.inkloom.model.Role;
import com.inkloom.repository.ApprovedSourceRepository;
import com.inkloom.repository.AuditEventRepository;
import com.inkloom.repository.BrandGuidelineRepository;
import com.inkloom.repository.BrandWorkspaceRepository;
import com.inkloom.repository.UserRepository;
import com.inkloom.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandWorkspaceService {

    private final BrandWorkspaceRepository workspaceRepository;
    private final BrandGuidelineRepository guidelineRepository;
    private final ApprovedSourceRepository sourceRepository;
    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public BrandWorkspaceService(BrandWorkspaceRepository workspaceRepository,
                                 BrandGuidelineRepository guidelineRepository,
                                 ApprovedSourceRepository sourceRepository,
                                 AuditEventRepository auditEventRepository,
                                 UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.guidelineRepository = guidelineRepository;
        this.sourceRepository = sourceRepository;
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
    }

    // Convenience constructor for tests
    public BrandWorkspaceService(BrandWorkspaceRepository workspaceRepository,
                                 BrandGuidelineRepository guidelineRepository,
                                 ApprovedSourceRepository sourceRepository,
                                 AuditEventRepository auditEventRepository) {
        this(workspaceRepository, guidelineRepository, sourceRepository, auditEventRepository, null);
    }

    public BrandWorkspaceResponse createWorkspace(BrandWorkspaceRequest request, String ownerUserId) {
        String slug = request.getName().toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
        if (workspaceRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis() % 1000;
        }

        BrandWorkspace workspace = new BrandWorkspace(
                request.getName(),
                slug,
                request.getIndustry(),
                request.getDescription(),
                ownerUserId != null ? ownerUserId : "system"
        );
        BrandWorkspace saved = workspaceRepository.save(workspace);

        if (ownerUserId != null && userRepository != null) {
            userRepository.findById(ownerUserId).ifPresent(u -> {
                if (!u.getWorkspaceIds().contains(saved.getId())) {
                    u.getWorkspaceIds().add(saved.getId());
                    userRepository.save(u);
                }
            });
        }

        // Initialize default guideline for the workspace
        BrandGuideline defaultGuideline = new BrandGuideline(saved.getId(), "v1.0", ownerUserId);
        defaultGuideline.getToneRules().add(new BrandGuideline.ToneRule(
                "Authoritative and Factual",
                "Project enterprise technical accuracy and avoid ungrounded hyperbole.",
                "Our distributed cluster guarantees high availability.",
                "Our tool is magic and unbeatable."
        ));
        guidelineRepository.save(defaultGuideline);

        auditEventRepository.save(new AuditEvent(
                saved.getId(),
                ownerUserId,
                "WORKSPACE_CREATED",
                "BrandWorkspace",
                saved.getId(),
                "Created workspace: " + saved.getName()
        ));

        return new BrandWorkspaceResponse(saved);
    }

    public List<BrandWorkspaceResponse> getAllWorkspaces() {
        return workspaceRepository.findAll().stream()
                .map(BrandWorkspaceResponse::new)
                .collect(Collectors.toList());
    }

    public List<BrandWorkspaceResponse> getWorkspacesForUser(UserPrincipal user) {
        if (user == null || user.getRole() == Role.ADMIN) {
            return getAllWorkspaces();
        }
        return workspaceRepository.findAll().stream()
                .filter(w -> (user.getWorkspaceIds() != null && user.getWorkspaceIds().contains(w.getId()))
                        || user.getId().equals(w.getOwnerUserId())
                        || (w.getMemberUserIds() != null && w.getMemberUserIds().contains(user.getId())))
                .map(BrandWorkspaceResponse::new)
                .collect(Collectors.toList());
    }

    public BrandWorkspaceResponse getWorkspaceById(String brandId) {
        BrandWorkspace workspace = workspaceRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("BrandWorkspace", "id", brandId));
        return new BrandWorkspaceResponse(workspace);
    }

    public BrandWorkspaceResponse updateWorkspace(String brandId, BrandWorkspaceRequest request) {
        BrandWorkspace workspace = workspaceRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("BrandWorkspace", "id", brandId));

        workspace.setName(request.getName());
        workspace.setIndustry(request.getIndustry());
        workspace.setDescription(request.getDescription());
        workspace.setUpdatedAt(Instant.now());

        return new BrandWorkspaceResponse(workspaceRepository.save(workspace));
    }

    public BrandGuidelineResponse getGuideline(String brandId) {
        BrandGuideline guideline = guidelineRepository.findFirstByBrandIdAndStatus(brandId, "ACTIVE")
                .orElseGet(() -> {
                    BrandGuideline newG = new BrandGuideline(brandId, "v1.0", "system");
                    return guidelineRepository.save(newG);
                });
        return new BrandGuidelineResponse(guideline);
    }

    public BrandGuidelineResponse updateGuideline(String brandId, BrandGuideline updated, String userId) {
        BrandGuideline guideline = guidelineRepository.findFirstByBrandIdAndStatus(brandId, "ACTIVE")
                .orElseGet(() -> new BrandGuideline(brandId, "v1.0", userId));

        guideline.setProhibitedExpressions(updated.getProhibitedExpressions());
        guideline.setRequiredPhrases(updated.getRequiredPhrases());
        guideline.setToneRules(updated.getToneRules());
        guideline.setWritingPreferences(updated.getWritingPreferences());
        guideline.setVersion(updated.getVersion() != null ? updated.getVersion() : guideline.getVersion());
        guideline.setUpdatedByUserId(userId);
        guideline.setUpdatedAt(Instant.now());

        BrandGuideline saved = guidelineRepository.save(guideline);

        auditEventRepository.save(new AuditEvent(
                brandId,
                userId,
                "GUIDELINE_UPDATED",
                "BrandGuideline",
                saved.getId(),
                "Updated guidelines to version: " + saved.getVersion()
        ));

        return new BrandGuidelineResponse(saved);
    }

    public List<ApprovedSourceResponse> getSources(String brandId) {
        return sourceRepository.findByBrandId(brandId).stream()
                .map(ApprovedSourceResponse::new)
                .collect(Collectors.toList());
    }

    public ApprovedSourceResponse createSource(String brandId, ApprovedSourceRequest request) {
        if (!workspaceRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("BrandWorkspace", "id", brandId);
        }

        ApprovedSource source = new ApprovedSource(
                brandId,
                request.getTitle(),
                request.getSourceType(),
                request.getOwner(),
                request.getExcerpt(),
                request.getReferenceUrl()
        );
        source.setVersion(request.getVersion() != null ? request.getVersion() : "1.0");

        ApprovedSource saved = sourceRepository.save(source);

        auditEventRepository.save(new AuditEvent(
                brandId,
                null,
                "SOURCE_CREATED",
                "ApprovedSource",
                saved.getId(),
                "Registered source: " + saved.getTitle()
        ));

        return new ApprovedSourceResponse(saved);
    }
}
