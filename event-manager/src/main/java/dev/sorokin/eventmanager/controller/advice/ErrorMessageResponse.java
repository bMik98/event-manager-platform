package dev.sorokin.eventmanager.controller.advice;

import java.time.LocalDateTime;

public record ErrorMessageResponse(
        String message,
        String detailedMessage,
        LocalDateTime timestamp
) {
    public static ErrorMessageResponse of(String message, String detailedMessage) {
        return new ErrorMessageResponse(message, detailedMessage, LocalDateTime.now());
    }
}
