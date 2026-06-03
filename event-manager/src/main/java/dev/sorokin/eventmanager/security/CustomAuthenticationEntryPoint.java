package dev.sorokin.eventmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sorokin.eventmanager.controller.advice.ErrorMessageResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, @NonNull AuthenticationException ex)
            throws IOException {
        log.error("Custom authentication entry point triggered for request: {}", request.getRequestURI(), ex);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var errorMessageResponse = ErrorMessageResponse.of("Unauthorized", ex.getMessage());
        String body = objectMapper.writeValueAsString(errorMessageResponse);
        response.getWriter().write(body);
    }
}
