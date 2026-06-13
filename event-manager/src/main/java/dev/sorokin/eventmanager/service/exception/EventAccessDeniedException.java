package dev.sorokin.eventmanager.service.exception;

public class EventAccessDeniedException extends ForbiddenOperationException {

    public EventAccessDeniedException(Long eventId) {
        super("Only the owner or an administrator may modify event %d".formatted(eventId));
    }
}
