package com.generic.logistics.service;

import com.generic.logistics.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "TXV5SW1wb3J0YW50ZUNsYXZlU2VjcmV0YVBhcmFTcHJpbmdCb290TmV2ZWwyU2VtaVNlbmlvcg==",
                86400000L
        );
        jwtService = new JwtService(properties);
    }

    @Test
    void shouldGenerateAndExtractUsernameSuccessfully() {
        String email = "driver@logistics.com";

        String token = jwtService.generateToken(email);
        String extractedEmail = jwtService.extractUsername(token);

        assertThat(token).isNotEmpty();
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void shouldValidateCorrectToken() {
        String email = "customer@logistics.com";
        String token = jwtService.generateToken(email);

        boolean isValid = jwtService.isTokenValid(token, email);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldExtractCustomRoleClaim() {
        String email = "admin@logistics.com";
        Map<String, Object> claims = Map.of("role", "LOGISTICS_ADMIN");

        String token = jwtService.generateToken(claims, email);
        String role = jwtService.extractRole(token);

        assertThat(role).isEqualTo("LOGISTICS_ADMIN");
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotMatchAndTokenIsValid() {
        String realOwnerEmail = "driver1@logistics.com";
        String token = jwtService.generateToken(realOwnerEmail);
        String distinctUserEmail = "driver2@logistics.com";

        boolean isValid = jwtService.isTokenValid(token, distinctUserEmail);

        assertThat(isValid).isFalse();
    }
}
