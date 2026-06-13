package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.service.model.Event;
import dev.sorokin.eventmanager.service.model.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Search filter for {@link Event}. No field is required; when all are empty every event is returned.
 * {@code name} is matched by exact equality, not substring (per the OpenAPI contract).
 */
@Schema(description = "Filter for searching events")
public record EventSearchRequestDto(

        @Schema(description = "Event name to match by exact equality", example = "Лекция")
        String name,

        @Schema(description = "Minimum number of places", example = "10")
        Integer placesMin,

        @Schema(description = "Maximum number of places", example = "100")
        Integer placesMax,

        @Schema(description = "Earliest start date and time (inclusive)", example = "2030-01-23T04:56:07.000+00:00")
        ZonedDateTime dateStartAfter,

        @Schema(description = "Latest start date and time (inclusive)", example = "2030-12-23T04:56:07.000+00:00")
        ZonedDateTime dateStartBefore,

        @Schema(description = "Minimum participation cost in rubles", example = "1000")
        Integer costMin,

        @Schema(description = "Maximum participation cost in rubles", example = "5000")
        Integer costMax,

        @Schema(description = "Minimum duration in minutes", example = "60")
        Integer durationMin,

        @Schema(description = "Maximum duration in minutes", example = "120")
        Integer durationMax,

        @Schema(description = "Location identifier to filter by", example = "5")
        Long locationId,

        @Schema(description = "Event status to filter by", implementation = EventStatus.class)
        EventStatus eventStatus

) implements Serializable {
}
