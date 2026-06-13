package dev.sorokin.eventmanager.service.model;

import java.time.ZonedDateTime;

public record Registration(
        Long id,
        Event event,
        UserAccount userAccount,
        ZonedDateTime createdAt
) {

    public Registration {
        if (event == null) {
            throw new IllegalArgumentException("Registration Event cannot be null");
        }
        if (userAccount == null) {
            throw new IllegalArgumentException("Registration UserAccount cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Registration createdAt cannot be null");
        }
    }

    public static Registration of(Event event, UserAccount userAccount) {
        return new Registration(null, event, userAccount, ZonedDateTime.now());
    }
}
