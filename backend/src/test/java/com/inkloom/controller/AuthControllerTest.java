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
                .andExpect(jsonPath("$.data.user.email").value("newuser@inkloom.ai"));
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
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token-login"));
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
}
