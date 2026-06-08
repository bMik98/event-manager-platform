package dev.sorokin.eventmanager.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "password.policy")
public record PasswordPolicyProperties(
        @DefaultValue("8") @Positive int minLength,
        @DefaultValue("255") @Positive @Max(255) int maxLength,
        @DefaultValue("true") boolean requireUppercase,
        @DefaultValue("true") boolean requireLowercase,
        @DefaultValue("true") boolean requireDigit,
        @DefaultValue("true") boolean requireSpecialChar
) {
}
