package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.service.exception.ConflictingOperationException;
import dev.sorokin.eventmanager.service.exception.RegistrationAlreadyExistsException;
import dev.sorokin.eventmanager.config.ControllerTestConfig;
import dev.sorokin.eventmanager.service.RegistrationService;
import dev.sorokin.eventmanager.common.exception.EventNotFoundException;
import dev.sorokin.eventmanager.common.exception.RegistrationNotFoundException;
import dev.sorokin.eventmanager.service.model.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static dev.sorokin.eventmanager.config.SecurityTestUsers.USER;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@Import(ControllerTestConfig.class)
class RegistrationControllerTest {

    private static final UserAccount OWNER = new UserAccount(10L, "owner", "hash", UserRole.USER, 25);
    private static final Location LOCATION = new Location(5L, "Main Hall", "123 Main St", 100, "Main venue");

    private static final Event EVENT = new Event(
            42L, "Lecture", OWNER, LOCATION,
            ZonedDateTime.parse("2999-01-01T10:00:00Z"),
            60, 10, 1200, 7, EventStatus.WAIT_START
    );

    @MockitoBean
    RegistrationService registrationService;

    @Autowired
    MockMvc mockMvc;

    @Nested
    class Register {

        static Stream<Arguments> registrationViolations() {
            return Stream.of(
                    arguments("event not in WAIT_START",
                            new ConflictingOperationException("Registration is not allowed for an event in status STARTED")),
                    arguments("event full",
                            new ConflictingOperationException("Event 42 has no free places")),
                    arguments("already registered",
                            new RegistrationAlreadyExistsException(42L, 10L))
            );
        }

        static Stream<Arguments> invalidIds() {
            return Stream.of(
                    arguments("negative", -1L),
                    arguments("zero", 0L)
            );
        }

        @Test
        void success_returns200() throws Exception {
            mockMvc.perform(post("/events/registrations/42").with(USER))
                    .andExpect(status().isOk());
        }

        @Test
        void eventNotFound_returns404() throws Exception {
            doThrow(new EventNotFoundException(999L))
                    .when(registrationService).register(999L, "user");

            mockMvc.perform(post("/events/registrations/999").with(USER))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("registrationViolations")
        void businessRuleViolation_returns400(String reason, RuntimeException exception) throws Exception {
            doThrow(exception)
                    .when(registrationService).register(42L, "user");

            mockMvc.perform(post("/events/registrations/42").with(USER))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Bad request"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidIds")
        void invalidId_returns400(String reason, long id) throws Exception {
            mockMvc.perform(post("/events/registrations/{id}", id).with(USER))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }
    }

    @Nested
    class Cancel {

        @Test
        void success_returns204() throws Exception {
            mockMvc.perform(delete("/events/registrations/cancel/42").with(USER))
                    .andExpect(status().isNoContent());
        }

        @Test
        void registrationNotFound_returns404() throws Exception {
            doThrow(new RegistrationNotFoundException(42L, 10L))
                    .when(registrationService).cancel(42L, "user");

            mockMvc.perform(delete("/events/registrations/cancel/42").with(USER))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"));
        }

        @Test
        void eventAlreadyStarted_returns400() throws Exception {
            doThrow(new ConflictingOperationException(
                    "Registration cannot be cancelled once the event has started or finished (status STARTED)"))
                    .when(registrationService).cancel(42L, "user");

            mockMvc.perform(delete("/events/registrations/cancel/42").with(USER))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Bad request"));
        }
    }

    @Nested
    class GetMy {

        @Test
        void returns200WithRegisteredEvents() throws Exception {
            when(registrationService.getRegisteredEvents("user")).thenReturn(List.of(EVENT));

            mockMvc.perform(get("/events/registrations/my").with(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(42))
                    .andExpect(jsonPath("$[0].name").value("Lecture"));
        }

        @Test
        void noRegistrations_returns200WithEmptyList() throws Exception {
            when(registrationService.getRegisteredEvents("user")).thenReturn(List.of());

            mockMvc.perform(get("/events/registrations/my").with(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
