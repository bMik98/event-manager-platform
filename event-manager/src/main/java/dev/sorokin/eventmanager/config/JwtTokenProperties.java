package dev.sorokin.eventmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtTokenProperties(
        String secret,
        long expirationInMillis
) {

    public JwtTokenProperties {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }

        if (expirationInMillis <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
    }
}
