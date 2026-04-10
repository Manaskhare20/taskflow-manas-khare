package com.taskflow.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void generatesAndParsesTokenWithClaims() {
        JwtProperties props = new JwtProperties();
        props.setSecret("unit-test-secret-must-be-at-least-32-bytes-long");
        props.setExpirationMs(60_000L);
        JwtService jwtService = new JwtService(props);

        UUID userId = UUID.randomUUID();
        String email = "user@example.com";
        String token = jwtService.generateToken(userId, email);

        Claims claims = jwtService.parseClaims(token);
        assertThat(claims.get("user_id", String.class)).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
    }
}
