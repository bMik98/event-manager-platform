package dev.sorokin.eventmanager.service.exception;

public class RegistrationAlreadyExistsException extends ItemAlreadyExistsException {

    public RegistrationAlreadyExistsException(Long eventId, Long userId) {
        super("Registration", "eventId/userId", eventId + "/" + userId);
    }
}
