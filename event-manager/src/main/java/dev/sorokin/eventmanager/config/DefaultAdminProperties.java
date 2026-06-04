package dev.sorokin.eventmanager.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = DefaultAdminProperties.PREFIX)
public record DefaultAdminProperties(
        @Max(255) @NotBlank String login,
        @Max(255) @NotBlank String password
) {
    public static final String PREFIX = "default-admin";
    public static final String LOGIN_PROPERTY = PREFIX + ".login";
}
