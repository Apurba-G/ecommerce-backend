package com.ecommerce.common;

import com.ecommerce.common.security.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate valid JWT token with claims and parse correctly")
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String username = "testuser";
        List<String> roles = List.of("ROLE_CUSTOMER");
        List<String> permissions = List.of("product:read", "user:read");

        String token = jwtUtils.generateAccessToken(userId, email, username, roles, permissions);

        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));

        Claims claims = jwtUtils.extractClaims(token);
        assertEquals(email, claims.getSubject());
        assertEquals(userId.toString(), claims.get("userId"));
        assertEquals(username, claims.get("username"));
        assertEquals(roles, claims.get("roles", List.class));
        assertEquals(permissions, claims.get("permissions", List.class));
    }

    @Test
    @DisplayName("Should return false for invalid or tampered token")
    void testInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid.jwt.token"));
    }
}
