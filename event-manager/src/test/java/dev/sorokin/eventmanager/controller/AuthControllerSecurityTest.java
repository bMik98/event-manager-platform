package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.ControllerSecurityTestConfig;
import dev.sorokin.eventmanager.config.PasswordPolicyProperties;
import dev.sorokin.eventmanager.controller.mapper.UserWebMapper;
import dev.sorokin.eventmanager.service.UserAccountService;
import dev.sorokin.eventmanager.service.model.UserAccount;
import dev.sorokin.eventmanager.service.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static dev.sorokin.eventmanager.config.SecurityTestUsers.*;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(ControllerSecurityTestConfig.class)
@EnableConfigurationProperties(PasswordPolicyProperties.class)
class AuthControllerSecurityTest {

    private static final String REGISTER_JSON = """
            {"login":"testuser","password":"Password1!","age":25}
            """;
    private static final String AUTH_JSON = """
            {"login":"testuser","password":"Password1!"}
            """;

    @MockitoBean
    UserAccountService userAccountService;
    @MockitoBean
    UserWebMapper mapper;
    @MockitoBean
    PasswordEncoder passwordEncoder;

    @Autowired
    MockMvc mockMvc;

    static MockHttpServletRequestBuilder register() {
        return post("/users")
                .contentType(APPLICATION_JSON)
                .content(REGISTER_JSON);
    }

    static MockHttpServletRequestBuilder authenticate() {
        return post("/users/auth")
                .contentType(APPLICATION_JSON)
                .content(AUTH_JSON);
    }

    static Stream<Arguments> securityMatrix() {
        return Stream.of(
                // POST /users — public (permitAll), returns 201 regardless of role
                arguments(named("ADMIN - POST /users - 201", register()), ADMIN, 201),
                arguments(named("USER  - POST /users - 201", register()), USER, 201),
                arguments(named("guest - POST /users - 201", register()), GUEST, 201),

                // POST /users/auth — public (permitAll), returns 200 regardless of role
                arguments(named("ADMIN - POST /users/auth - 200", authenticate()), ADMIN, 200),
                arguments(named("USER  - POST /users/auth - 200", authenticate()), USER, 200),
                arguments(named("guest - POST /users/auth - 200", authenticate()), GUEST, 200)
        );
    }

    @BeforeEach
    void stubUserService() {
        when(userAccountService.getUserByLogin(any()))
                .thenReturn(new UserAccount(1L, "testuser", "hashed", UserRole.USER, 25));
        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("securityMatrix")
    void securityMatrix(MockHttpServletRequestBuilder request,
                        RequestPostProcessor auth,
                        int expectedStatus) throws Exception {
        mockMvc.perform(request.with(auth))
                .andExpect(status().is(expectedStatus));
    }
}
