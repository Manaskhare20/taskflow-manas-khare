package com.taskflow.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "taskflow.jwt")
public class JwtProperties {

    /**
     * HS256 secret; must be strong in production.
     */
    private String secret;

    /**
     * Token TTL in milliseconds (default 24h).
     */
    private long expirationMs = 86_400_000L;
}
