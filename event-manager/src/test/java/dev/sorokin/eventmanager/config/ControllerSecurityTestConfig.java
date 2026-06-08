package dev.sorokin.eventmanager.config;

import dev.sorokin.eventmanager.security.CustomAccessDeniedHandler;
import dev.sorokin.eventmanager.security.CustomAuthenticationEntryPoint;
import dev.sorokin.eventmanager.security.JwtAuthFilter;
import dev.sorokin.eventmanager.security.JwtAuthService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import tools.jackson.databind.json.JsonMapper;

@TestConfiguration
@Import({
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
public class ControllerSecurityTestConfig {

    @Bean
    JsonMapper jsonMapper() {
        return JsonMapper.builder().build();
    }

    @Bean
    JwtAuthService jwtAuthService() {
        return Mockito.mock(JwtAuthService.class);
    }

    @Bean
    UserDetailsService userDetailsService() {
        return Mockito.mock(UserDetailsService.class);
    }

    @Bean
    FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(jwtAuthFilter);
        registration.setEnabled(false);
        return registration;
    }
}
