package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.ControllerSecurityTestConfig;
import dev.sorokin.eventmanager.controller.mapper.LocationWebMapper;
import dev.sorokin.eventmanager.service.LocationService;
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

import static dev.sorokin.eventmanager.config.SecurityTestUsers.ADMIN;
import static dev.sorokin.eventmanager.config.SecurityTestUsers.GUEST;
import static dev.sorokin.eventmanager.config.SecurityTestUsers.USER;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
@Import(ControllerSecurityTestConfig.class)
class LocationControllerSecurityTest {

    private static final String VALID_JSON = """
            {"name":"Hall","address":"Addr","capacity":50}
            """;

    @MockitoBean
    LocationService locationService;
    @MockitoBean
    LocationWebMapper mapper;

    @Autowired
    MockMvc mockMvc;

    static MockHttpServletRequestBuilder createLocation() {
        return post("/locations").contentType(APPLICATION_JSON).content(VALID_JSON);
    }

    static MockHttpServletRequestBuilder updateLocation() {
        return put("/locations/1").contentType(APPLICATION_JSON).content(VALID_JSON);
    }

    static Stream<Arguments> securityMatrix() {
        return Stream.of(
                // GET /locations — USER and ADMIN allowed, anonymous denied
                arguments(named("ADMIN - GET /locations - 200", get("/locations")), ADMIN, 200),
                arguments(named("USER  - GET /locations - 200", get("/locations")), USER, 200),
                arguments(named("guest - GET /locations - 401", get("/locations")), GUEST, 401),

                // GET /locations/1 — USER and ADMIN allowed, anonymous denied
                arguments(named("USER  - GET /locations/1 - 200", get("/locations/1")), USER, 200),
                arguments(named("ADMIN - GET /locations/1 - 200", get("/locations/1")), ADMIN, 200),
                arguments(named("guest - GET /locations/1 - 401", get("/locations/1")), GUEST, 401),

                // POST /locations — ADMIN only
                arguments(named("ADMIN - POST /locations - 201", createLocation()), ADMIN, 201),
                arguments(named("USER  - POST /locations - 403", createLocation()), USER, 403),
                arguments(named("guest - POST /locations - 401", createLocation()), GUEST, 401),

                // PUT /locations/1 — ADMIN only
                arguments(named("ADMIN - PUT /locations/1 - 200", updateLocation()), ADMIN, 200),
                arguments(named("USER  - PUT /locations/1 - 403", updateLocation()), USER, 403),
                arguments(named("guest - PUT /locations/1 - 401", updateLocation()), GUEST, 401),

                // DELETE /locations/1 — ADMIN only
                arguments(named("ADMIN - DELETE /locations/1 - 204", delete("/locations/1")), ADMIN, 204),
                arguments(named("USER  - DELETE /locations/1 - 403", delete("/locations/1")), USER, 403),
                arguments(named("guest - DELETE /locations/1 - 401", delete("/locations/1")), GUEST, 401)
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
