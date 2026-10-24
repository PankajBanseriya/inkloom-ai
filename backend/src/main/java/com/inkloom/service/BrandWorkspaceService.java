package com.inkloom.service;

import com.inkloom.dto.request.ApprovedSourceRequest;
import com.inkloom.dto.request.BrandWorkspaceRequest;
import com.inkloom.dto.request.InviteMemberRequest;
import com.inkloom.dto.response.ApprovedSourceResponse;
import com.inkloom.dto.response.BrandGuidelineResponse;
import com.inkloom.dto.response.BrandWorkspaceResponse;
import com.inkloom.dto.response.UserResponse;
import com.inkloom.exception.ResourceNotFoundException;
import com.inkloom.model.ApprovedSource;
import com.inkloom.model.AuditEvent;
import com.inkloom.model.BrandGuideline;
import com.inkloom.model.BrandWorkspace;
import com.inkloom.model.Role;
import com.inkloom.model.User;
import com.inkloom.repository.ApprovedSourceRepository;
import com.inkloom.repository.AuditEventRepository;
import com.inkloom.repository.BrandGuidelineRepository;
import com.inkloom.repository.BrandWorkspaceRepository;
import com.inkloom.repository.UserRepository;
import com.inkloom.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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

    public ApprovedSourceResponse updateSource(String brandId, String sourceId, ApprovedSourceRequest request) {
        ApprovedSource source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovedSource", "id", sourceId));

        if (!source.getBrandId().equals(brandId)) {
            throw new ResourceNotFoundException("ApprovedSource", "brandId", brandId);
        }

        source.setTitle(request.getTitle());
        source.setSourceType(request.getSourceType());
        source.setOwner(request.getOwner());
        source.setExcerpt(request.getExcerpt());
        source.setReferenceUrl(request.getReferenceUrl());
        if (request.getVersion() != null) {
            source.setVersion(request.getVersion());
        }
        source.setUpdatedAt(Instant.now());

        ApprovedSource saved = sourceRepository.save(source);

        auditEventRepository.save(new AuditEvent(
                brandId,
                null,
                "SOURCE_UPDATED",
                "ApprovedSource",
                saved.getId(),
                "Updated source: " + saved.getTitle()
        ));

        return new ApprovedSourceResponse(saved);
    }

    public void deleteSource(String brandId, String sourceId) {
        ApprovedSource source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovedSource", "id", sourceId));

        if (!source.getBrandId().equals(brandId)) {
            throw new ResourceNotFoundException("ApprovedSource", "brandId", brandId);
        }

        sourceRepository.delete(source);

        auditEventRepository.save(new AuditEvent(
                brandId,
                null,
                "SOURCE_DELETED",
                "ApprovedSource",
                sourceId,
                "Deleted source: " + source.getTitle()
        ));
    }

    public List<UserResponse> getWorkspaceMembers(String brandId) {
        BrandWorkspace workspace = workspaceRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("BrandWorkspace", "id", brandId));

        if (userRepository == null) {
            return Collections.emptyList();
        }

        return userRepository.findAll().stream()
                .filter(u -> (u.getWorkspaceIds() != null && u.getWorkspaceIds().contains(brandId))
                        || (workspace.getMemberUserIds() != null && workspace.getMemberUserIds().contains(u.getId()))
                        || brandId.equals(workspace.getId()) && u.getId().equals(workspace.getOwnerUserId()))
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    public UserResponse inviteMember(String brandId, InviteMemberRequest request, String inviterUserId) {
        BrandWorkspace workspace = workspaceRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("BrandWorkspace", "id", brandId));

        if (userRepository == null) {
            throw new IllegalStateException("User repository not available");
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseGet(() -> {
                    String initialHash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                            .encode("ChangeMe123!");
                    User newUser = new User(
                            request.getEmail().toLowerCase().trim(),
                            initialHash,
                            request.getFullName().trim(),
                            request.getRole() != null ? request.getRole() : Role.CONTENT_CREATOR
                    );
                    newUser.getWorkspaceIds().add(brandId);
                    return userRepository.save(newUser);
                });

        if (!user.getWorkspaceIds().contains(brandId)) {
            user.getWorkspaceIds().add(brandId);
            if (request.getRole() != null) {
                user.setRole(request.getRole());
            }
            user = userRepository.save(user);
        }

        if (workspace.getMemberUserIds() == null) {
            workspace.setMemberUserIds(new ArrayList<>());
        }
        if (!workspace.getMemberUserIds().contains(user.getId())) {
            workspace.getMemberUserIds().add(user.getId());
            workspaceRepository.save(workspace);
        }

        auditEventRepository.save(new AuditEvent(
                brandId,
                inviterUserId,
                "MEMBER_INVITED",
                "User",
                user.getId(),
                "Invited member " + user.getFullName() + " (" + user.getRole() + ") to workspace"
        ));

        return new UserResponse(user);
    }
}
