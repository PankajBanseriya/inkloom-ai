package com.inkloom.service;

import com.inkloom.dto.request.LoginRequest;
import com.inkloom.dto.request.RegisterRequest;
import com.inkloom.dto.response.AuthResponse;
import com.inkloom.dto.response.UserResponse;
import com.inkloom.exception.DuplicateResourceException;
import com.inkloom.exception.ResourceNotFoundException;
import com.inkloom.exception.UnauthorizedAccessException;
import com.inkloom.model.AuditEvent;
import com.inkloom.model.User;
import com.inkloom.repository.AuditEventRepository;
import com.inkloom.repository.UserRepository;
import com.inkloom.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditEventRepository auditEventRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditEventRepository auditEventRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditEventRepository = auditEventRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new DuplicateResourceException("A user with email " + request.getEmail() + " already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getEmail().toLowerCase(), encodedPassword, request.getFullName(), request.getRole());
        User saved = userRepository.save(user);

        String token = tokenProvider.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name(), saved.getWorkspaceIds());

        auditEventRepository.save(new AuditEvent(
                null,
                saved.getId(),
                "USER_REGISTERED",
                "User",
                saved.getId(),
                "New user registered: " + saved.getEmail()
        ));

        return new AuthResponse(token, new UserResponse(saved));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedAccessException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getWorkspaceIds());

        auditEventRepository.save(new AuditEvent(
                null,
                user.getId(),
                "USER_LOGIN",
                "User",
                user.getId(),
                "User authenticated: " + user.getEmail()
        ));

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
