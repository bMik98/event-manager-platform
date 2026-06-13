package dev.sorokin.eventmanager.common.exception;

public class LocationNotFoundException extends ItemNotFoundException {

    public LocationNotFoundException(Long id) {
        super("Location", id);
    }
}
