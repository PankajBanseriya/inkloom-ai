package com.inkloom.service;

import com.inkloom.dto.request.LoginRequest;
import com.inkloom.dto.request.RegisterRequest;
import com.inkloom.dto.response.AuthResponse;
import com.inkloom.dto.response.UserResponse;
import com.inkloom.exception.DuplicateResourceException;
import com.inkloom.exception.ResourceNotFoundException;
import com.inkloom.exception.UnauthorizedAccessException;
import com.inkloom.model.AuditEvent;
import com.inkloom.model.BrandWorkspace;
import com.inkloom.model.Role;
import com.inkloom.model.User;
import com.inkloom.repository.AuditEventRepository;
import com.inkloom.repository.BrandWorkspaceRepository;
import com.inkloom.repository.UserRepository;
import com.inkloom.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditEventRepository auditEventRepository;
    private final BrandWorkspaceRepository brandWorkspaceRepository;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditEventRepository auditEventRepository,
                       @Autowired(required = false) BrandWorkspaceRepository brandWorkspaceRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditEventRepository = auditEventRepository;
        this.brandWorkspaceRepository = brandWorkspaceRepository;
    }

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditEventRepository auditEventRepository) {
        this(userRepository, passwordEncoder, tokenProvider, auditEventRepository, null);
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new DuplicateResourceException("A user with email " + request.getEmail() + " already exists.");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.CONTENT_CREATOR;
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getEmail().toLowerCase(), encodedPassword, request.getFullName(), role);

        List<String> workspaceIds = new ArrayList<>();
        if (brandWorkspaceRepository != null && request.getWorkspaceName() != null && !request.getWorkspaceName().isBlank()) {
            try {
                String slug = request.getWorkspaceName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
                if (slug.isBlank()) slug = "ws-" + System.currentTimeMillis();
                BrandWorkspace workspace = new BrandWorkspace(
                        request.getWorkspaceName().trim(),
                        slug,
                        "General Enterprise",
                        "Primary workspace for " + request.getFullName(),
                        user.getId()
                );
                BrandWorkspace savedWorkspace = brandWorkspaceRepository.save(workspace);
                workspaceIds.add(savedWorkspace.getId());
            } catch (Exception ignored) {
                // Non-blocking workspace initialization
            }
        }
        user.setWorkspaceIds(workspaceIds);

        User saved = userRepository.save(user);

        String roleName = saved.getRole() != null ? saved.getRole().name() : role.name();
        List<String> userWorkspaces = saved.getWorkspaceIds() != null ? saved.getWorkspaceIds() : workspaceIds;
        String token = tokenProvider.generateToken(saved.getId(), saved.getEmail(), roleName, userWorkspaces);

        try {
            auditEventRepository.save(new AuditEvent(
                    !userWorkspaces.isEmpty() ? userWorkspaces.get(0) : null,
                    saved.getId(),
                    "USER_REGISTERED",
                    "User",
                    saved.getId(),
                    "New user registered: " + saved.getEmail()
            ));
        } catch (Exception ignored) {
            // Transient audit logging errors do not block user registration
        }

        return new AuthResponse(token, new UserResponse(saved));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedAccessException("Invalid email or password");
        }

        String roleName = user.getRole() != null ? user.getRole().name() : Role.CONTENT_CREATOR.name();
        List<String> workspaceIds = user.getWorkspaceIds() != null ? user.getWorkspaceIds() : List.of();
        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), roleName, workspaceIds);

        try {
            auditEventRepository.save(new AuditEvent(
                    !workspaceIds.isEmpty() ? workspaceIds.get(0) : null,
                    user.getId(),
                    "USER_LOGIN",
                    "User",
                    user.getId(),
                    "User authenticated: " + user.getEmail()
            ));
        } catch (Exception ignored) {
            // Transient audit logging errors do not block login
        }

        return new AuthResponse(token, new UserResponse(user));
    }

    public UserResponse getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedAccessException("Authorization token is missing");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!tokenProvider.validateToken(token)) {
            throw new UnauthorizedAccessException("Invalid or expired authentication token");
        }
        String userId = tokenProvider.getClaimsFromToken(token).getSubject();
        return getUserById(userId);
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return new UserResponse(user);
    }
}
