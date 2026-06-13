package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.ControllerSecurityTestConfig;
import dev.sorokin.eventmanager.controller.mapper.EventWebMapper;
import dev.sorokin.eventmanager.service.RegistrationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@Import(ControllerSecurityTestConfig.class)
class RegistrationControllerSecurityTest {

    @MockitoBean
    RegistrationService registrationService;
    @MockitoBean
    EventWebMapper eventMapper;

    @Autowired
    MockMvc mockMvc;

    static Stream<Arguments> securityMatrix() {
        return Stream.of(
                // POST /events/registrations/1 — USER only (ADMIN is staff, not a participant)
                arguments(named("USER  - POST register - 200", post("/events/registrations/1")), USER, 200),
                arguments(named("ADMIN - POST register - 403", post("/events/registrations/1")), ADMIN, 403),
                arguments(named("guest - POST register - 401", post("/events/registrations/1")), GUEST, 401),

                // DELETE /events/registrations/cancel/1 — USER only
                arguments(named("USER  - DELETE cancel - 204", delete("/events/registrations/cancel/1")), USER, 204),
                arguments(named("ADMIN - DELETE cancel - 403", delete("/events/registrations/cancel/1")), ADMIN, 403),
                arguments(named("guest - DELETE cancel - 401", delete("/events/registrations/cancel/1")), GUEST, 401),

                // GET /events/registrations/my — USER only
                arguments(named("USER  - GET my - 200", get("/events/registrations/my")), USER, 200),
                arguments(named("ADMIN - GET my - 403", get("/events/registrations/my")), ADMIN, 403),
                arguments(named("guest - GET my - 401", get("/events/registrations/my")), GUEST, 401)
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
