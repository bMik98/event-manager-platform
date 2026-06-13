package dev.sorokin.eventmanager.service.model;

import java.time.ZonedDateTime;

public record EventCreate(
        String name,
        Integer maxPlaces,
        ZonedDateTime startAt,
        Integer cost,
        Integer durationMinutes,
        Long locationId
) {
}
