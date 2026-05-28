package dev.sorokin.eventmanager.service.model;

import dev.sorokin.eventmanager.common.EventManagerConstants;

import java.util.Optional;

public record Location(
        Long id,
        String name,
        String address,
        int capacity,
        String description
) {

    public Location {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Event Location name must not be blank");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Event Location address must not be blank");
        }
        if (capacity < EventManagerConstants.MIN_LOCATION_CAPACITY) {
            throw new IllegalArgumentException(
                    "Event Location capacity must be at least " + EventManagerConstants.MIN_LOCATION_CAPACITY
            );
        }
    }

    public Optional<String> getOptionalDescription() {
        return Optional.ofNullable(description);
    }
}
