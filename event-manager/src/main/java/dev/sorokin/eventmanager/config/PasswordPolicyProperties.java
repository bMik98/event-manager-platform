package dev.sorokin.eventmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "password.policy")
public record PasswordPolicyProperties(
        @DefaultValue("8") int minLength,
        @DefaultValue("255") int maxLength,
        @DefaultValue("true") boolean requireUppercase,
        @DefaultValue("true") boolean requireLowercase,
        @DefaultValue("true") boolean requireDigit,
        @DefaultValue("true") boolean requireSpecialChar
) {
}
