package dev.sorokin.eventmanager.service.exception;

public class ConflictingOperationException extends RuntimeException {

    public ConflictingOperationException(String message) {
        super(message);
    }
}
