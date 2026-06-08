package dev.sorokin.eventmanager.service.model;

import dev.sorokin.eventmanager.common.EventManagerConstants;

public record UserAccount(
        Long id,
        String login,
        String passwordHash,
        UserRole role,
        Integer age
) {
    public UserAccount {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("User login must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("User password hash must not be blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("User role must not be null");
        }
        if (age == null || age < EventManagerConstants.MIN_USER_AGE) {
            throw new IllegalArgumentException(
                    "User age must be " + EventManagerConstants.MIN_USER_AGE + " or older"
            );
        }
    }
}
