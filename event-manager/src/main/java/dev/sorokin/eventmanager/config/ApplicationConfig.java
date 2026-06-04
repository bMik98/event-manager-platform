package dev.sorokin.eventmanager.config;

import dev.sorokin.eventmanager.controller.validation.PasswordValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ApplicationConfig {

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    @Bean
    public PasswordValidator passwordValidator(PasswordPolicyProperties passwordPolicyProperties) {
        return new PasswordValidator(passwordPolicyProperties);
    }
}
