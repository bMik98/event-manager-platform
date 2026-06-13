package dev.sorokin.eventmanager.service.model;

import java.time.ZonedDateTime;

public record Event(
        Long id,
        String name,
        UserAccount owner,
        Location location,
        ZonedDateTime startAt,
        int durationMinutes,
        int maxPlaces,
        int cost,
        int occupiedPlaces,
        EventStatus status
) {
    @SuppressWarnings("java:S1541")
    public Event {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Event name cannot be null or blank");
        }
        if (owner == null) {
            throw new IllegalArgumentException("Event owner cannot be null");
        }
        if (location == null) {
            throw new IllegalArgumentException("Event location cannot be null");
        }
        if (startAt == null) {
            throw new IllegalArgumentException("Event startAt cannot be null");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Event durationMinutes must be positive");
        }
        if (maxPlaces <= 0) {
            throw new IllegalArgumentException("Event maxPlaces must be positive");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Event cost must not be negative");
        }
        if (occupiedPlaces < 0) {
            throw new IllegalArgumentException("Event occupiedPlaces cannot be negative");
        }
        if (occupiedPlaces > maxPlaces) {
            throw new IllegalArgumentException("Event occupiedPlaces cannot exceed maxPlaces");
        }
        if (status == null) {
            throw new IllegalArgumentException("Event status cannot be null");
        }
    }
}
