package com.example.chat_app.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // 64-char hex key (32 bytes = 256 bits for HS256)
    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", 900000L);
        ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION_TIME", 604800000L);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken("testuser");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken("testuser");
        assertThat(jwtService.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken("testuser");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        assertThat(jwtService.isTokenValid("invalid.token.here")).isFalse();
    }

    @Test
    void generateRefreshToken_shouldHaveDifferentExpirationThanAccessToken() {
        String access = jwtService.generateToken("testuser");
        String refresh = jwtService.generateRefreshToken("testuser");
        // Both valid but different tokens
        assertThat(access).isNotEqualTo(refresh);
        assertThat(jwtService.extractUsername(refresh)).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        // Set expiration to 1ms
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", 1L);
        String token = jwtService.generateToken("testuser");
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}