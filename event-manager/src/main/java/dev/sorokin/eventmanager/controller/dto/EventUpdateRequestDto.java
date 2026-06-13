package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.common.EventManagerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Schema(description = "Data for updating an event")
public record EventUpdateRequestDto(

        @Schema(description = "New event name", example = "Новое название")
        String name,

        @Schema(description = "New number of places. Must fit the location and exceed already-registered participants", example = "100")
        @Positive
        Integer maxPlaces,

        @Schema(description = "New start date and time. Must be in the future", example = "2030-01-23T04:56:07.000+00:00")
        @Future
        ZonedDateTime date,

        @Schema(description = "New participation cost in rubles", example = "3000")
        @Min(EventManagerConstants.MIN_EVENT_COST)
        Integer cost,

        @Schema(description = "New duration in minutes", example = "60")
        @Min(EventManagerConstants.MIN_EVENT_DURATION_IN_MINUTES)
        Integer duration,

        @Schema(description = "New location identifier. Must fit registered participants and maxPlaces", example = "50")
        Long locationId

) implements Serializable {
}
