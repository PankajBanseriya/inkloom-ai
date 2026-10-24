package com.inkloom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.AuthResponse;
import com.inkloom.dto.response.UserResponse;
import com.inkloom.model.Role;
import com.inkloom.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {JacksonConfig.class})
class InstantSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("ObjectMapper successfully serializes java.time.Instant in ApiResponse and AuthResponse")
    void testInstantSerializationInApiResponse() throws Exception {
        User user = new User("instant.test@acme.internal", "hashed", "Instant Tester", Role.CONTENT_CREATOR);
        user.setId("u-instant-1");
        user.setCreatedAt(Instant.parse("2026-10-24T18:30:00Z"));

        UserResponse userResponse = new UserResponse(user);
        AuthResponse authResponse = new AuthResponse("mock-jwt-token-instant", userResponse);
        ApiResponse<AuthResponse> apiResponse = ApiResponse.ok("Authentication successful", authResponse);

        // This must not throw HttpMessageConversionException or InvalidDefinitionException
        String json = objectMapper.writeValueAsString(apiResponse);

        assertNotNull(json);
        assertTrue(json.contains("\"timestamp\":\""), "Response should serialize timestamp as ISO-8601 string");
        assertTrue(json.contains("\"createdAt\":\"2026-10-24T18:30:00Z\""), "UserResponse.createdAt should be ISO-8601 string");
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"token\":\"mock-jwt-token-instant\""));
    }

    @Test
    @DisplayName("ObjectMapper deserializes ISO-8601 strings back into java.time.Instant")
    void testInstantDeserialization() throws Exception {
        String json = "{\"timestamp\":\"2026-10-24T20:00:00Z\",\"success\":true,\"message\":\"OK\"}";

        ApiResponse<?> response = objectMapper.readValue(json, ApiResponse.class);

        assertNotNull(response);
        assertEquals(Instant.parse("2026-10-24T20:00:00Z"), response.getTimestamp());
    }
}
