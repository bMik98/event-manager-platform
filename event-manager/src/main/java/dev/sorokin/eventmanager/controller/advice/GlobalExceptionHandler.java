package dev.sorokin.eventmanager.controller.advice;

import dev.sorokin.eventmanager.common.exception.ItemNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type";
    public static final String NOT_FOUND = "Not found";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn(VALIDATION_FAILED, ex);
        String details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorMessageResponse body = ErrorMessageResponse.of(VALIDATION_FAILED, details);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleEntityNotFoundException(ItemNotFoundException ex) {
        log.warn(NOT_FOUND, ex);
        ErrorMessageResponse body = ErrorMessageResponse.of(NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessageResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn(VALIDATION_FAILED, ex);
        String details = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath().toString() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        ErrorMessageResponse body = ErrorMessageResponse.of(VALIDATION_FAILED, details);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorMessageResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        MediaType mediaType = ex.getContentType();
        String unsupportedType = (mediaType != null)
                ? mediaType.toString()
                : "unknown";
        String detailedMessage = "Media type '%s' is not supported".formatted(unsupportedType);
        ErrorMessageResponse body = ErrorMessageResponse.of(UNSUPPORTED_MEDIA_TYPE, detailedMessage);
        log.warn(UNSUPPORTED_MEDIA_TYPE, ex);
        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessageResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        ErrorMessageResponse body = ErrorMessageResponse.of("Bad request", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageResponse> handleException(Exception ex) {
        log.error("Unexpected server error", ex);
        ErrorMessageResponse body = ErrorMessageResponse.of(
                "Internal server error",
                "Unexpected server error. See logs for details."
        );
        return ResponseEntity.internalServerError().body(body);
    }
}
