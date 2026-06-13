package dev.sorokin.eventmanager.config;

import dev.sorokin.eventmanager.security.JwtAuthFilter;
import dev.sorokin.eventmanager.service.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String USER = UserRole.USER.name();
    private static final String ADMIN = UserRole.ADMIN.name();
    private static final String[] ANY = {USER, ADMIN};

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    @SuppressWarnings({"java:S1192", "java:S5612"})
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(configurer -> configurer
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/users/auth").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole(ADMIN)

                        .requestMatchers(HttpMethod.GET, "/locations/**").hasAnyRole(ANY)
                        .requestMatchers("/locations/**").hasRole(ADMIN)

                        .requestMatchers("/events/registrations/**").hasRole(USER)

                        .requestMatchers(HttpMethod.POST, "/events/search").hasAnyRole(ANY)
                        .requestMatchers(HttpMethod.GET, "/events/my").hasRole(USER)
                        .requestMatchers(HttpMethod.POST, "/events").hasRole(USER)
                        .requestMatchers(HttpMethod.GET, "/events/**").hasAnyRole(ANY)
                        .requestMatchers(HttpMethod.PUT, "/events/**").hasAnyRole(ANY)
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasAnyRole(ANY)

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
