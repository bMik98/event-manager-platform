package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.common.EventManagerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Schema(description = "Data for creating a new event")
public record EventCreateRequestDto(

        @Schema(description = "Event name", example = "Лекция по Java")
        @NotBlank
        String name,

        @Schema(description = "Maximum number of places at the event", example = "10")
        @NotNull
        @Positive
        Integer maxPlaces,

        @Schema(description = "Date and time the event starts. Must be in the future", example = "2030-01-23T04:56:07.000+00:00")
        @NotNull
        @Future
        ZonedDateTime date,

        @Schema(description = "Participation cost in rubles", example = "1200")
        @NotNull
        @Min(EventManagerConstants.MIN_EVENT_COST)
        Integer cost,

        @Schema(description = "Duration in minutes", example = "60")
        @NotNull
        @Min(EventManagerConstants.MIN_EVENT_DURATION_IN_MINUTES)
        Integer duration,

        @Schema(description = "Identifier of the location where the event takes place", example = "10")
        @NotNull
        Long locationId

) implements Serializable {
}
