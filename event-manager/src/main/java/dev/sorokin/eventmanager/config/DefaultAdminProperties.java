package dev.sorokin.eventmanager.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = DefaultAdminProperties.PREFIX)
public record DefaultAdminProperties(
        @Size(max = 255) @NotBlank String login,
        @Size(max = 255) @NotBlank String password
) {
    public static final String PREFIX = "default-admin";
    public static final String LOGIN_PROPERTY = PREFIX + ".login";
}
