package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.common.EventManagerConstants;
import dev.sorokin.eventmanager.service.model.Location;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * Create / Update DTO for {@link Location}
 */
public record LocationRequestDto(
        @NotBlank String name,
        @NotBlank String address,
        @Min(EventManagerConstants.MIN_LOCATION_CAPACITY) int capacity,
        String description
) implements Serializable {
}
