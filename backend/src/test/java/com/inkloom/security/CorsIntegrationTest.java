package com.inkloom.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkloom.controller.AuthController;
import com.inkloom.controller.BrandController;
import com.inkloom.controller.HealthController;
import com.inkloom.dto.request.LoginRequest;
import com.inkloom.dto.response.AuthResponse;
import com.inkloom.dto.response.UserResponse;
import com.inkloom.model.Role;
import com.inkloom.service.AuthService;
import com.inkloom.service.BrandWorkspaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, BrandController.class, HealthController.class})
@Import(com.inkloom.config.TestSecurityConfig.class)
class CorsIntegrationTest {

    private static final String VERCEL_ORIGIN = "https://inkloom-ai-xi.vercel.app";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private BrandWorkspaceService brandWorkspaceService;

    @Test
    @DisplayName("OPTIONS preflight to /api/v1/auth/login from Vercel frontend succeeds with CORS headers")
    void testPreflightApiV1AuthLogin() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, VERCEL_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, VERCEL_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
    }

    @Test
    @DisplayName("OPTIONS preflight to /auth/login (without /api/v1 prefix) from Vercel frontend succeeds with CORS headers")
    void testPreflightRootAuthLogin() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, VERCEL_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, VERCEL_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("OPTIONS preflight to protected endpoint /api/v1/brands succeeds without authentication")
    void testPreflightProtectedEndpointSucceedsUnauthenticated() throws Exception {
        mockMvc.perform(options("/api/v1/brands")
                        .header(HttpHeaders.ORIGIN, VERCEL_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, VERCEL_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login from Vercel frontend returns CORS headers on success")
    void testPostApiV1AuthLoginSuccess() throws Exception {
        UserResponse user = new UserResponse();
        user.setId("u-1");
        user.setEmail("test@inkloom.internal");
        user.setFullName("Test User");
        user.setRole(Role.ADMIN);
        user.setWorkspaceIds(List.of("brand-01"));

        AuthResponse authResponse = new AuthResponse("test-jwt-token-xyz", user);
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        LoginRequest loginRequest = new LoginRequest("test@inkloom.internal", "ValidPass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, VERCEL_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, VERCEL_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("POST /auth/login (without /api/v1) from Vercel frontend returns CORS headers on success")
    void testPostRootAuthLoginSuccess() throws Exception {
        UserResponse user = new UserResponse();
        user.setId("u-1");
        user.setEmail("test@inkloom.internal");
        user.setFullName("Test User");
        user.setRole(Role.ADMIN);
        user.setWorkspaceIds(List.of("brand-01"));

        AuthResponse authResponse = new AuthResponse("test-jwt-token-xyz", user);
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        LoginRequest loginRequest = new LoginRequest("test@inkloom.internal", "ValidPass123!");

        mockMvc.perform(post("/auth/login")
                        .header(HttpHeaders.ORIGIN, VERCEL_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, VERCEL_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("GET /api/v1/health from Vercel frontend returns CORS headers")
    void testGetHealthCors() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .header(HttpHeaders.ORIGIN, VERCEL_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, VERCEL_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint preserves CORS header on 401 response")
    void testUnauthenticatedProtectedEndpointPreservesCorsHeader() throws Exception {
        mockMvc.perform(get("/api/v1/brands")
                        .header(HttpHeaders.ORIGIN, VERCEL_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, VERCEL_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("Disallowed origin preflight request does not receive Access-Control-Allow-Origin")
    void testDisallowedOriginPreflight() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, "https://malicious-untrusted-site.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
