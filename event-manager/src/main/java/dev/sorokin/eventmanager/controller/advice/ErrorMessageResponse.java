package dev.sorokin.eventmanager.controller.advice;

import java.time.LocalDateTime;

public record ErrorMessageResponse(
        String message,
        String detailedMessage,
        LocalDateTime timestamp
) {
    public ErrorMessageResponse(String message, String detailedMessage) {
        this(message, detailedMessage, LocalDateTime.now());
    }
}
