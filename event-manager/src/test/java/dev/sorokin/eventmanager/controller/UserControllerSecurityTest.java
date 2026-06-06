package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.ControllerSecurityTestConfig;
import dev.sorokin.eventmanager.controller.mapper.UserWebMapper;
import dev.sorokin.eventmanager.service.UserAccountService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static dev.sorokin.eventmanager.config.SecurityTestUsers.*;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(ControllerSecurityTestConfig.class)
class UserControllerSecurityTest {

    @MockitoBean
    UserAccountService userAccountService;
    @MockitoBean
    UserWebMapper mapper;

    @Autowired
    MockMvc mockMvc;

    static Stream<Arguments> securityMatrix() {
        return Stream.of(
                // GET /users — ADMIN only
                arguments(named("ADMIN - GET /users - 200", get("/users")), ADMIN, 200),
                arguments(named("USER  - GET /users - 403", get("/users")), USER, 403),
                arguments(named("guest - GET /users - 401", get("/users")), GUEST, 401),

                // GET /users/1 — ADMIN only
                arguments(named("ADMIN - GET /users/1 - 200", get("/users/1")), ADMIN, 200),
                arguments(named("USER  - GET /users/1 - 403", get("/users/1")), USER, 403),
                arguments(named("guest - GET /users/1 - 401", get("/users/1")), GUEST, 401)
        );
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
