package com.inkloom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkloom.config.CorsConfig;
import com.inkloom.config.SecurityConfig;
import com.inkloom.dto.request.LoginRequest;
import com.inkloom.dto.request.RegisterRequest;
import com.inkloom.dto.response.AuthResponse;
import com.inkloom.dto.response.UserResponse;
import com.inkloom.model.Role;
import com.inkloom.model.User;
import com.inkloom.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.inkloom.config.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/register returns 201 Created on valid input")
    void testRegisterEndpoint() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser@inkloom.ai", "Password123!", "Jane Doe", Role.ADMIN);
        User user = new User("newuser@inkloom.ai", "hashed", "Jane Doe", Role.ADMIN);
        user.setId("u-1");
        AuthResponse authResponse = new AuthResponse("mock-jwt-token-xyz", new UserResponse(user));

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token-xyz"))
                .andExpect(jsonPath("$.data.user.email").value("newuser@inkloom.ai"))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.data.user.createdAt").isString());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 200 OK on valid credentials")
    void testLoginEndpoint() throws Exception {
        LoginRequest request = new LoginRequest("user@inkloom.ai", "SecretPass123!");
        User user = new User("user@inkloom.ai", "hashed", "Jane Doe", Role.REVIEWER);
        user.setId("u-2");
        AuthResponse authResponse = new AuthResponse("mock-jwt-token-login", new UserResponse(user));

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token-login"))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.data.user.createdAt").isString());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "u-2", roles = {"REVIEWER"})
    @DisplayName("GET /api/v1/auth/me returns current user info")
    void testMeEndpointWithHeader() throws Exception {
        User user = new User("user@inkloom.ai", "hashed", "Jane Doe", Role.REVIEWER);
        user.setId("u-2");

        when(authService.getUserById("u-2")).thenReturn(new UserResponse(user));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("X-User-Id", "u-2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@inkloom.ai"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with duplicate email returns 409 Conflict")
    void testRegisterDuplicateEmailReturns409() throws Exception {
        RegisterRequest request = new RegisterRequest("existing@inkloom.ai", "Password123!", "Jane Doe", Role.ADMIN);

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new com.inkloom.exception.DuplicateResourceException("A user with email existing@inkloom.ai already exists."));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with invalid credentials returns 401 Unauthorized")
    void testLoginInvalidCredentialsReturns401() throws Exception {
        LoginRequest request = new LoginRequest("user@inkloom.ai", "WrongPassword!");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new com.inkloom.exception.UnauthorizedAccessException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.details").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Access to /error is permitted without authentication, preventing 401 masking")
    void testErrorEndpointPermittedAnonymously() throws Exception {
        // Calling /error without JWT token should NOT be rejected with 401 Unauthorized by Spring Security
        mockMvc.perform(get("/error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(
                        401,
                        result.getResponse().getStatus(),
                        "The /error dispatch must not be blocked by 401 Unauthorized in Spring Security 6"
                ));
    }
}
