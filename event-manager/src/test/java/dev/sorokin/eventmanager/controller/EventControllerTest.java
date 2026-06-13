package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.ControllerTestConfig;
import dev.sorokin.eventmanager.service.EventService;
import dev.sorokin.eventmanager.service.exception.InvalidCommandException;
import dev.sorokin.eventmanager.service.exception.EventAccessDeniedException;
import dev.sorokin.eventmanager.common.exception.EventNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(ControllerTestConfig.class)
class EventControllerTest {

    private static final UserAccount OWNER = new UserAccount(10L, "user", "hash", UserRole.USER, 25);
    private static final Location LOCATION = new Location(5L, "Main Hall", "123 Main St", 100, "Main venue");

    private static final Event EVENT = new Event(
            42L, "Lecture", OWNER, LOCATION,
            ZonedDateTime.parse("2999-01-01T10:00:00Z"),
            60, 10, 1200, 7, EventStatus.WAIT_START
    );

    private static final String VALID_CREATE_JSON = """
            {"name":"Lecture","maxPlaces":10,"date":"2999-01-01T10:00:00Z","cost":1200,"duration":60,"locationId":5}
            """;

    @MockitoBean
    EventService eventService;

    @Autowired
    MockMvc mockMvc;

    @Nested
    class Create {

        static Stream<Arguments> invalidRequests() {
            return Stream.of(
                    arguments("blank name", """
                            {"name":"","maxPlaces":10,"date":"2999-01-01T10:00:00Z","cost":1200,"duration":60,"locationId":5}
                            """),
                    arguments("missing maxPlaces", """
                            {"name":"L","date":"2999-01-01T10:00:00Z","cost":1200,"duration":60,"locationId":5}
                            """),
                    arguments("date in the past", """
                            {"name":"L","maxPlaces":10,"date":"2000-01-01T10:00:00Z","cost":1200,"duration":60,"locationId":5}
                            """),
                    arguments("cost below 1", """
                            {"name":"L","maxPlaces":10,"date":"2999-01-01T10:00:00Z","cost":0,"duration":60,"locationId":5}
                            """),
                    arguments("duration below 30", """
                            {"name":"L","maxPlaces":10,"date":"2999-01-01T10:00:00Z","cost":1200,"duration":10,"locationId":5}
                            """),
                    arguments("missing locationId", """
                            {"name":"L","maxPlaces":10,"date":"2999-01-01T10:00:00Z","cost":1200,"duration":60}
                            """)
            );
        }

        @Test
        void success_returns201WithBody() throws Exception {
            when(eventService.createEvent(any(), eq("user"))).thenReturn(EVENT);

            mockMvc.perform(post("/events").with(USER)
                            .contentType(APPLICATION_JSON).content(VALID_CREATE_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(42))
                    .andExpect(jsonPath("$.name").value("Lecture"))
                    .andExpect(jsonPath("$.ownerId").value(10))
                    .andExpect(jsonPath("$.locationId").value(5))
                    .andExpect(jsonPath("$.maxPlaces").value(10))
                    .andExpect(jsonPath("$.occupiedPlaces").value(7))
                    .andExpect(jsonPath("$.cost").value(1200))
                    .andExpect(jsonPath("$.duration").value(60))
                    .andExpect(jsonPath("$.status").value("WAIT_START"))
                    .andExpect(jsonPath("$.date").isNotEmpty());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidRequests")
        void invalidBody_returns400WithValidationMessage(String reason, String json) throws Exception {
            mockMvc.perform(post("/events").with(USER)
                            .contentType(APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }
    }

    @Nested
    class GetById {

        static Stream<Arguments> invalidIds() {
            return Stream.of(
                    arguments("negative", -1L),
                    arguments("zero", 0L)
            );
        }

        @Test
        void exists_returns200WithBody() throws Exception {
            when(eventService.getEvent(42L)).thenReturn(EVENT);

            mockMvc.perform(get("/events/42").with(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(42))
                    .andExpect(jsonPath("$.name").value("Lecture"))
                    .andExpect(jsonPath("$.ownerId").value(10))
                    .andExpect(jsonPath("$.locationId").value(5))
                    .andExpect(jsonPath("$.status").value("WAIT_START"));
        }

        @Test
        void notFound_returns404WithErrorResponse() throws Exception {
            when(eventService.getEvent(999L)).thenThrow(new EventNotFoundException(999L));

            mockMvc.perform(get("/events/999").with(USER))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"))
                    .andExpect(jsonPath("$.detailedMessage").isNotEmpty());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidIds")
        void invalidId_returns400(String reason, long id) throws Exception {
            mockMvc.perform(get("/events/{id}", id).with(USER))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }
    }

    @Nested
    class Update {

        @Test
        void success_returns200WithUpdatedBody() throws Exception {
            var updated = new Event(
                    42L, "Updated", OWNER, LOCATION,
                    ZonedDateTime.parse("2999-01-01T10:00:00Z"),
                    90, 20, 3000, 7, EventStatus.WAIT_START
            );
            when(eventService.updateEvent(eq(42L), any(), eq("user"))).thenReturn(updated);

            mockMvc.perform(put("/events/42").with(USER)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"name":"Updated","maxPlaces":20,"cost":3000,"duration":90}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated"))
                    .andExpect(jsonPath("$.maxPlaces").value(20))
                    .andExpect(jsonPath("$.cost").value(3000))
                    .andExpect(jsonPath("$.duration").value(90));
        }

        @Test
        void notFound_returns404() throws Exception {
            when(eventService.updateEvent(eq(999L), any(), eq("user")))
                    .thenThrow(new EventNotFoundException(999L));

            mockMvc.perform(put("/events/999").with(USER)
                            .contentType(APPLICATION_JSON).content("{\"name\":\"X\"}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"));
        }

        @Test
        void notOwner_returns403() throws Exception {
            when(eventService.updateEvent(eq(42L), any(), eq("user")))
                    .thenThrow(new EventAccessDeniedException(42L));

            mockMvc.perform(put("/events/42").with(USER)
                            .contentType(APPLICATION_JSON).content("{\"name\":\"X\"}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Forbidden"));
        }
    }

    @Nested
    class Delete {

        @Test
        void success_returns204() throws Exception {
            mockMvc.perform(delete("/events/42").with(USER))
                    .andExpect(status().isNoContent());
        }

        @Test
        void notFound_returns404() throws Exception {
            doThrow(new EventNotFoundException(999L)).when(eventService).cancelEvent(999L, "user");

            mockMvc.perform(delete("/events/999").with(USER))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"));
        }

        @Test
        void cannotCancelInCurrentStatus_returns400() throws Exception {
            doThrow(new InvalidCommandException("Event 42 cannot be cancelled in status STARTED"))
                    .when(eventService).cancelEvent(42L, "user");

            mockMvc.perform(delete("/events/42").with(USER))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Bad request"));
        }

        @Test
        void notOwner_returns403() throws Exception {
            doThrow(new EventAccessDeniedException(42L)).when(eventService).cancelEvent(42L, "user");

            mockMvc.perform(delete("/events/42").with(USER))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Forbidden"));
        }
    }

    @Nested
    class Search {

        @Test
        void returns200WithMatchingEvents() throws Exception {
            when(eventService.searchEvents(any())).thenReturn(List.of(EVENT));

            mockMvc.perform(post("/events/search").with(USER)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"name":"Lecture","placesMin":5}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(42))
                    .andExpect(jsonPath("$[0].name").value("Lecture"));
        }

        @Test
        void emptyFilter_returns200WithAll() throws Exception {
            when(eventService.searchEvents(any())).thenReturn(List.of(EVENT));

            mockMvc.perform(post("/events/search").with(USER)
                            .contentType(APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    class GetMy {

        @Test
        void returns200WithOwnEvents() throws Exception {
            when(eventService.getMyEvents("user")).thenReturn(List.of(EVENT));

            mockMvc.perform(get("/events/my").with(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].ownerId").value(10));
        }

        @Test
        void noEvents_returns200WithEmptyList() throws Exception {
            when(eventService.getMyEvents("user")).thenReturn(List.of());

            mockMvc.perform(get("/events/my").with(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
