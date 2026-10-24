package com.inkloom.service;

import com.inkloom.dto.request.LoginRequest;
import com.inkloom.dto.request.RegisterRequest;
import com.inkloom.dto.response.AuthResponse;
import com.inkloom.exception.DuplicateResourceException;
import com.inkloom.exception.UnauthorizedAccessException;
import com.inkloom.model.Role;
import com.inkloom.model.User;
import com.inkloom.repository.AuditEventRepository;
import com.inkloom.repository.UserRepository;
import com.inkloom.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private com.inkloom.repository.BrandWorkspaceRepository brandWorkspaceRepository;

    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        tokenProvider = new JwtTokenProvider(
                "inkloom_development_secret_key_minimum_256_bits_for_hmac_sha256_secure_key_12345",
                86400000L
        );
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider, auditEventRepository, brandWorkspaceRepository);
    }

    @Test
    @DisplayName("register creates user with encoded password and returns JWT token")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("test@inkloom.ai", "SecretPass123!", "Jane Doe", Role.ADMIN);

        when(userRepository.existsByEmail("test@inkloom.ai")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId("user-123");
            return u;
        });

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("test@inkloom.ai", response.getUser().getEmail());
        assertEquals(Role.ADMIN, response.getUser().getRole());
        verify(auditEventRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("register throws DuplicateResourceException for duplicate email")
    void testRegisterDuplicate() {
        RegisterRequest request = new RegisterRequest("exists@inkloom.ai", "SecretPass123!", "Jane Doe", Role.REVIEWER);

        when(userRepository.existsByEmail("exists@inkloom.ai")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login returns valid token for correct credentials")
    void testLoginSuccess() {
        String hashed = passwordEncoder.encode("ValidPassword!");
        User user = new User("user@inkloom.ai", hashed, "Alex Brand", Role.BRAND_MANAGER);
        user.setId("u-456");

        when(userRepository.findByEmail("user@inkloom.ai")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("user@inkloom.ai", "ValidPassword!");
        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("Alex Brand", response.getUser().getFullName());
    }

    @Test
    @DisplayName("login throws UnauthorizedAccessException for incorrect password")
    void testLoginWrongPassword() {
        String hashed = passwordEncoder.encode("CorrectPassword");
        User user = new User("user@inkloom.ai", hashed, "Alex Brand", Role.BRAND_MANAGER);

        when(userRepository.findByEmail("user@inkloom.ai")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("user@inkloom.ai", "WrongPassword");

        assertThrows(UnauthorizedAccessException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("login throws UnauthorizedAccessException for non-existent email")
    void testLoginNonExistentEmail() {
        when(userRepository.findByEmail("nobody@inkloom.ai")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nobody@inkloom.ai", "AnyPassword");

        assertThrows(UnauthorizedAccessException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("register with workspace name provisions brand workspace and assigns to user")
    void testRegisterWithWorkspaceProvisioning() {
        RegisterRequest request = new RegisterRequest("creator@inkloom.ai", "SecretPass123!", "Elena Rostova", Role.BRAND_MANAGER, "Acme Cloud");

        when(userRepository.existsByEmail("creator@inkloom.ai")).thenReturn(false);
        when(brandWorkspaceRepository.save(any(com.inkloom.model.BrandWorkspace.class))).thenAnswer(invocation -> {
            com.inkloom.model.BrandWorkspace ws = invocation.getArgument(0);
            ws.setId("brand-acme");
            return ws;
        });
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId("u-created");
            return u;
        });

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("creator@inkloom.ai", response.getUser().getEmail());
        assertTrue(response.getUser().getWorkspaceIds().contains("brand-acme"));
        verify(brandWorkspaceRepository, times(1)).save(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("demo credentials authenticate successfully across all four enterprise roles")
    void testDemoCredentialsAuthenticateAcrossRoles() {
        String encodedPass = passwordEncoder.encode("demo-pass");
        Role[] roles = {Role.ADMIN, Role.BRAND_MANAGER, Role.REVIEWER, Role.CONTENT_CREATOR};
        String[] emails = {
                "alex.cross@acme.internal",
                "elena.rostova@acme.internal",
                "sarah.jenkins@acme.internal",
                "marcus.vance@acme.internal"
        };

        for (int i = 0; i < roles.length; i++) {
            Role role = roles[i];
            String email = emails[i];

            User demoUser = new User(email, encodedPass, "Demo User " + role, role);
            demoUser.setId("demo-id-" + i);
            demoUser.setWorkspaceIds(java.util.List.of("brand-01"));

            when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(demoUser));

            LoginRequest request = new LoginRequest(email, "demo-pass");
            AuthResponse response = authService.login(request);

            assertNotNull(response, "Auth response should not be null for role " + role);
            assertNotNull(response.getToken(), "Token should be present for role " + role);
            assertEquals(role, response.getUser().getRole(), "Role should match for " + email);
            assertTrue(tokenProvider.validateToken(response.getToken()), "Generated JWT token should be valid");
        }
    }
}
