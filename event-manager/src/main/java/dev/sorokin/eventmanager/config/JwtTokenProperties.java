package dev.sorokin.eventmanager.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtTokenProperties(
        @NotBlank String secret,
        @Positive long expirationInMillis
) {
}
