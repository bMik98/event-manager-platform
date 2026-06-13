package dev.sorokin.eventmanager.common.exception;

public class EventNotFoundException extends ItemNotFoundException {

    public EventNotFoundException(Long id) {
        super("Event", id);
    }
}
