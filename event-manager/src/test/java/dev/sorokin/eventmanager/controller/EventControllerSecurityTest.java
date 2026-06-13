package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.ControllerSecurityTestConfig;
import dev.sorokin.eventmanager.controller.mapper.EventWebMapper;
import dev.sorokin.eventmanager.service.EventService;
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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(ControllerSecurityTestConfig.class)
class EventControllerSecurityTest {

    private static final String CREATE_JSON = """
            {"name":"Lecture","maxPlaces":10,"date":"2999-01-01T10:00:00Z","cost":1200,"duration":60,"locationId":5}
            """;

    @MockitoBean
    EventService eventService;
    @MockitoBean
    EventWebMapper mapper;

    @Autowired
    MockMvc mockMvc;

    static MockHttpServletRequestBuilder createEvent() {
        return post("/events").contentType(APPLICATION_JSON).content(CREATE_JSON);
    }

    static MockHttpServletRequestBuilder updateEvent() {
        return put("/events/1").contentType(APPLICATION_JSON).content("{}");
    }

    static MockHttpServletRequestBuilder searchEvents() {
        return post("/events/search").contentType(APPLICATION_JSON).content("{}");
    }

    static Stream<Arguments> securityMatrix() {
        return Stream.of(
                // POST /events — USER only (ADMIN is platform staff, not an organizer)
                arguments(named("USER  - POST /events - 201", createEvent()), USER, 201),
                arguments(named("ADMIN - POST /events - 403", createEvent()), ADMIN, 403),
                arguments(named("guest - POST /events - 401", createEvent()), GUEST, 401),

                // GET /events/1 — USER and ADMIN allowed
                arguments(named("USER  - GET /events/1 - 200", get("/events/1")), USER, 200),
                arguments(named("ADMIN - GET /events/1 - 200", get("/events/1")), ADMIN, 200),
                arguments(named("guest - GET /events/1 - 401", get("/events/1")), GUEST, 401),

                // PUT /events/1 — USER and ADMIN allowed (ownership enforced in the service)
                arguments(named("USER  - PUT /events/1 - 200", updateEvent()), USER, 200),
                arguments(named("ADMIN - PUT /events/1 - 200", updateEvent()), ADMIN, 200),
                arguments(named("guest - PUT /events/1 - 401", updateEvent()), GUEST, 401),

                // DELETE /events/1 — USER and ADMIN allowed
                arguments(named("USER  - DELETE /events/1 - 204", delete("/events/1")), USER, 204),
                arguments(named("ADMIN - DELETE /events/1 - 204", delete("/events/1")), ADMIN, 204),
                arguments(named("guest - DELETE /events/1 - 401", delete("/events/1")), GUEST, 401),

                // POST /events/search — USER and ADMIN allowed
                arguments(named("USER  - POST /events/search - 200", searchEvents()), USER, 200),
                arguments(named("ADMIN - POST /events/search - 200", searchEvents()), ADMIN, 200),
                arguments(named("guest - POST /events/search - 401", searchEvents()), GUEST, 401),

                // GET /events/my — USER only
                arguments(named("USER  - GET /events/my - 200", get("/events/my")), USER, 200),
                arguments(named("ADMIN - GET /events/my - 403", get("/events/my")), ADMIN, 403),
                arguments(named("guest - GET /events/my - 401", get("/events/my")), GUEST, 401)
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
