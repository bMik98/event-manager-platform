package dev.sorokin.eventmanager.config;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public final class SecurityTestUsers {

    public static final RequestPostProcessor ADMIN = user("admin").roles("ADMIN");
    public static final RequestPostProcessor USER = user("user").roles("USER");
    public static final RequestPostProcessor GUEST = anonymous();

    private SecurityTestUsers() { /* static usage */ }
}
