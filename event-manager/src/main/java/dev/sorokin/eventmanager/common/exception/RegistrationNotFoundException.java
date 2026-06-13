package dev.sorokin.eventmanager.common.exception;

public class RegistrationNotFoundException extends ItemNotFoundException {

    public RegistrationNotFoundException(Long eventId, Long userId) {
        super("Registration", "eventId/userId", eventId + "/" + userId);
    }
}
