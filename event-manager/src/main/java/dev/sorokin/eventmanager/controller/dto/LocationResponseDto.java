package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.service.model.Location;

import java.io.Serializable;

/**
 * Response DTO for {@link Location}
 */
public record LocationResponseDto(
        long id,
        String name,
        String address,
        int capacity,
        String description
) implements Serializable {
}
