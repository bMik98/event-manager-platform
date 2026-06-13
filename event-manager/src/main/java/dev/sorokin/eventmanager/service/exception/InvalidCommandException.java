package dev.sorokin.eventmanager.service.exception;

/**
 * Raised when a command is rejected by a business rule given the current state of the data
 * (e.g. an event is full, or a new capacity would not fit the already-registered participants).
 * This is a client error, mapped to HTTP 400 by the web layer.
 *
 * <p>Unlike {@link IllegalArgumentException} — which signals a programming bug (a method received an
 * argument it should never have been given) and is mapped to HTTP 500 — this exception is part of the
 * expected control flow and carries a message safe to return to the caller.
 */
public class InvalidCommandException extends RuntimeException {

    public InvalidCommandException(String message) {
        super(message);
    }
}
