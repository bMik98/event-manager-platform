package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.ControllerTestConfig;
import dev.sorokin.eventmanager.service.LocationService;
import dev.sorokin.eventmanager.service.exception.LocationNotFoundException;
import dev.sorokin.eventmanager.service.model.Location;
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

import java.util.List;
import java.util.stream.Stream;

import static dev.sorokin.eventmanager.config.SecurityTestUsers.ADMIN;
import static dev.sorokin.eventmanager.config.SecurityTestUsers.USER;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
@Import(ControllerTestConfig.class)
class LocationControllerTest {

    private static final Location LOCATION =
            new Location(1L, "Main Hall", "123 Main St", 100, "Main venue");

    private static final String VALID_JSON = """
            {"name":"Main Hall","address":"123 Main St","capacity":100,"description":"Main venue"}
            """;

    @MockitoBean LocationService locationService;

    @Autowired MockMvc mockMvc;

    @Nested
    class GetAll {

        @Test
        void returns200WithList() throws Exception {
            when(locationService.getAllLocations()).thenReturn(List.of(LOCATION));

            mockMvc.perform(get("/locations").with(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Main Hall"))
                    .andExpect(jsonPath("$[0].address").value("123 Main St"))
                    .andExpect(jsonPath("$[0].capacity").value(100))
                    .andExpect(jsonPath("$[0].description").value("Main venue"));
        }

        @Test
        void returns200WithEmptyList() throws Exception {
            when(locationService.getAllLocations()).thenReturn(List.of());

            mockMvc.perform(get("/locations").with(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class GetById {

        @Test
        void exists_returns200WithCorrectBody() throws Exception {
            when(locationService.getLocation(1L)).thenReturn(LOCATION);

            mockMvc.perform(get("/locations/1").with(USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Main Hall"))
                    .andExpect(jsonPath("$.address").value("123 Main St"))
                    .andExpect(jsonPath("$.capacity").value(100))
                    .andExpect(jsonPath("$.description").value("Main venue"));
        }

        @Test
        void notFound_returns404WithErrorResponse() throws Exception {
            when(locationService.getLocation(999L))
                    .thenThrow(new LocationNotFoundException(999L));

            mockMvc.perform(get("/locations/999").with(USER))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"))
                    .andExpect(jsonPath("$.detailedMessage").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidIds")
        void invalidId_returns400WithValidationMessage(String reason, long id) throws Exception {
            mockMvc.perform(get("/locations/{id}", id).with(USER))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        static Stream<Arguments> invalidIds() {
            return Stream.of(
                    arguments("negative", -1L),
                    arguments("zero", 0L)
            );
        }
    }

    @Nested
    class Create {

        @Test
        void success_returns201WithBody() throws Exception {
            when(locationService.createLocation(any())).thenReturn(LOCATION);

            mockMvc.perform(post("/locations").with(ADMIN)
                            .contentType(APPLICATION_JSON).content(VALID_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Main Hall"))
                    .andExpect(jsonPath("$.address").value("123 Main St"))
                    .andExpect(jsonPath("$.capacity").value(100))
                    .andExpect(jsonPath("$.description").value("Main venue"));
        }

        @Test
        void withoutDescription_returns201WithNullDescription() throws Exception {
            when(locationService.createLocation(any()))
                    .thenReturn(new Location(2L, "Hall B", "456 Side St", 50, null));

            mockMvc.perform(post("/locations").with(ADMIN)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"name":"Hall B","address":"456 Side St","capacity":50}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.description").value((Object) null));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidRequests")
        void invalidBody_returns400WithValidationMessage(String reason, String json) throws Exception {
            mockMvc.perform(post("/locations").with(ADMIN)
                            .contentType(APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        static Stream<Arguments> invalidRequests() {
            return Stream.of(
                    arguments("blank name", """
                            {"name":"","address":"Addr","capacity":50}
                            """),
                    arguments("blank address", """
                            {"name":"Hall","address":"","capacity":50}
                            """),
                    arguments("capacity too low", """
                            {"name":"Hall","address":"Addr","capacity":1}
                            """)
            );
        }
    }

    @Nested
    class Update {

        @Test
        void success_returns200WithUpdatedBody() throws Exception {
            var updated = new Location(1L, "Updated Hall", "999 New St", 200, "Updated desc");
            when(locationService.updateLocation(eq(1L), any())).thenReturn(updated);

            mockMvc.perform(put("/locations/1").with(ADMIN)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"name":"Updated Hall","address":"999 New St","capacity":200,"description":"Updated desc"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Updated Hall"))
                    .andExpect(jsonPath("$.address").value("999 New St"))
                    .andExpect(jsonPath("$.capacity").value(200))
                    .andExpect(jsonPath("$.description").value("Updated desc"));
        }

        @Test
        void notFound_returns404WithErrorResponse() throws Exception {
            when(locationService.updateLocation(eq(999L), any()))
                    .thenThrow(new LocationNotFoundException(999L));

            mockMvc.perform(put("/locations/999").with(ADMIN)
                            .contentType(APPLICATION_JSON).content(VALID_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"))
                    .andExpect(jsonPath("$.detailedMessage").isNotEmpty());
        }

        @Test
        void invalidBody_returns400WithValidationMessage() throws Exception {
            mockMvc.perform(put("/locations/1").with(ADMIN)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"name":"","address":"Addr","capacity":50}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }
    }

    @Nested
    class Delete {

        @Test
        void success_returns204() throws Exception {
            mockMvc.perform(delete("/locations/1").with(ADMIN))
                    .andExpect(status().isNoContent());
        }

        @Test
        void notFound_returns404WithErrorResponse() throws Exception {
            doThrow(new LocationNotFoundException(999L))
                    .when(locationService).deleteLocationById(999L);

            mockMvc.perform(delete("/locations/999").with(ADMIN))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"))
                    .andExpect(jsonPath("$.detailedMessage").isNotEmpty());
        }
    }
}
