package dev.sorokin.eventmanager.service.exception;

/**
 * Raised when an authenticated caller is not permitted to perform an operation on a specific resource
 * (instance-level authorization). Mapped to HTTP 403 by the web layer.
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
