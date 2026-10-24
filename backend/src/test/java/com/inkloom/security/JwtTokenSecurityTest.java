package com.inkloom.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenSecurityTest {

    private JwtTokenProvider tokenProvider;
    private final String secretKey = "test_super_secure_key_for_inkloom_testing_minimum_256_bits_123456";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secretKey, 3600000L); // 1 hour
    }

    @Test
    @DisplayName("generateToken produces valid token and extracts claims accurately")
    void testValidTokenGenerationAndParsing() {
        String token = tokenProvider.generateToken("user-100", "test@inkloom.ai", "BRAND_MANAGER", List.of("brand-a", "brand-b"));

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("user-100", tokenProvider.getUserIdFromToken(token));
        assertEquals("test@inkloom.ai", tokenProvider.getClaimsFromToken(token).get("email"));
        assertEquals("BRAND_MANAGER", tokenProvider.getClaimsFromToken(token).get("role"));
        assertFalse(tokenProvider.isExpired(token));
    }

    @Test
    @DisplayName("Expired token is rejected by validateToken and flagged as expired")
    void testExpiredTokenHandling() {
        String expiredToken = tokenProvider.generateExpiredToken("user-100", "test@inkloom.ai", "REVIEWER");

        assertNotNull(expiredToken);
        assertFalse(tokenProvider.validateToken(expiredToken));
        assertTrue(tokenProvider.isExpired(expiredToken));
    }

    @Test
    @DisplayName("Tampered or malformed token is rejected by validateToken")
    void testInvalidTokenHandling() {
        String token = tokenProvider.generateToken("user-100", "test@inkloom.ai", "REVIEWER");
        String tamperedToken = token + "xyz";

        assertFalse(tokenProvider.validateToken(tamperedToken));
        assertFalse(tokenProvider.validateToken("not-a-valid-jwt"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }
}
