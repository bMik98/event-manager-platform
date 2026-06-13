package dev.sorokin.eventmanager.service.model;

import java.time.ZonedDateTime;

public record EventSearchFilter(
        String name,
        Integer placesMin,
        Integer placesMax,
        ZonedDateTime dateStartAfter,
        ZonedDateTime dateStartBefore,
        Integer costMin,
        Integer costMax,
        Integer durationMin,
        Integer durationMax,
        Long locationId,
        EventStatus eventStatus
) {
}
