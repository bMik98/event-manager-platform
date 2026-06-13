package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.service.model.Event;
import dev.sorokin.eventmanager.service.model.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Response DTO for {@link Event} (EventDto in the OpenAPI contract).
 * The owner and location associations are exposed as flat identifiers.
 */
@Schema(description = "Event data")
public record EventResponseDto(

        @Schema(description = "Unique event identifier", example = "42")
        Long id,

        @Schema(description = "Event name", example = "Лекция по Java")
        String name,

        @Schema(description = "Identifier of the user who created the event", example = "10")
        Long ownerId,

        @Schema(description = "Maximum number of places at the event", example = "10")
        Integer maxPlaces,

        @Schema(description = "Number of occupied places (the organizer is not counted)", example = "7")
        Integer occupiedPlaces,

        @Schema(description = "Date and time the event starts", example = "2030-01-23T04:56:07.000+00:00")
        ZonedDateTime date,

        @Schema(description = "Participation cost in rubles", example = "1200")
        Integer cost,

        @Schema(description = "Duration in minutes", example = "60")
        Integer duration,

        @Schema(description = "Identifier of the location where the event takes place", example = "5")
        Long locationId,

        @Schema(description = "Current event status", implementation = EventStatus.class)
        EventStatus status

) implements Serializable {
}
