package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.service.model.Registration;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Response DTO for {@link Registration}.
 * <p>
 * Note: the current OpenAPI contract does not define a registration schema — the registration
 * endpoints are driven by the {@code eventId} path variable and {@code GET /events/registrations/my}
 * returns a list of {@link EventResponseDto}. This DTO is provided as an addition for exposing
 * registration details should an endpoint need it.
 */
@Schema(description = "Registration of a user for an event")
public record RegistrationResponseDto(

        @Schema(description = "Unique registration identifier", example = "100")
        Long id,

        @Schema(description = "Identifier of the event the user registered for", example = "42")
        Long eventId,

        @Schema(description = "Identifier of the registered user", example = "10")
        Long userId,

        @Schema(description = "Date and time the registration was created", example = "2030-01-20T12:00:00.000+00:00")
        ZonedDateTime createdAt

) implements Serializable {
}
