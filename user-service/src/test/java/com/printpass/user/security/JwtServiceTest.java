package com.printpass.user.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey;
    private Key signingKey;
    private final String testEmail = "test@printpass.com";
    private final String testUserId = "123e4567-e89b-12d3-a456-426614174000";
    private final String testRole = "PRINT_SHOP";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Generate a valid 256-bit Base64 secret key
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
        secretKey = Encoders.BASE64.encode(keyBytes);
        signingKey = Keys.hmacShaKeyFor(keyBytes);

        // Inject the secret key into the service
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    }

    private String createToken(String email, String userId, String role, Date expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void extractEmail_ShouldReturnEmail() {
        String token = createToken(testEmail, testUserId, testRole, new Date(System.currentTimeMillis() + 100000));
        assertEquals(testEmail, jwtService.extractEmail(token));
    }

    @Test
    void extractRole_ShouldReturnRole() {
        String token = createToken(testEmail, testUserId, testRole, new Date(System.currentTimeMillis() + 100000));
        assertEquals(testRole, jwtService.extractRole(token));
    }

    @Test
    void extractUserId_ShouldReturnUserId() {
        String token = createToken(testEmail, testUserId, testRole, new Date(System.currentTimeMillis() + 100000));
        assertEquals(testUserId, jwtService.extractUserId(token));
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        String token = createToken(testEmail, testUserId, testRole, new Date(System.currentTimeMillis() + 100000));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsExpired() {
        // Token expired 1 hour ago
        String token = createToken(testEmail, testUserId, testRole, new Date(System.currentTimeMillis() - 3600000));
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsMalformed() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token"));
    }
}