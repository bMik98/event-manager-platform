package dev.sorokin.eventmanager.controller.advice;

import dev.sorokin.eventmanager.common.exception.ItemAlreadyExistsException;
import dev.sorokin.eventmanager.common.exception.ItemNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type";
    public static final String NOT_FOUND = "Not found";
    public static final String CONFLICT = "Conflict";

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
    public ResponseEntity<ErrorMessageResponse> handleItemNotFoundException(ItemNotFoundException ex) {
        log.warn(NOT_FOUND, ex);
        ErrorMessageResponse body = ErrorMessageResponse.of(NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ItemAlreadyExistsException.class)
    public ResponseEntity<ErrorMessageResponse> handleItemAlreadyExistsException(ItemAlreadyExistsException ex) {
        log.warn(CONFLICT, ex);
        ErrorMessageResponse body = ErrorMessageResponse.of(CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorMessageResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        log.warn(VALIDATION_FAILED, ex);
        ErrorMessageResponse body = ErrorMessageResponse.of(VALIDATION_FAILED, ex.getMessage());
        return ResponseEntity.badRequest().body(body);
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

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        ErrorMessageResponse body = ErrorMessageResponse.of(NOT_FOUND,
                "No handler found for %s %s".formatted(ex.getHttpMethod(), ex.getRequestURL()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("No resource found: {}", ex.getResourcePath());
        ErrorMessageResponse body = ErrorMessageResponse.of(NOT_FOUND,
                "No resource found: %s".formatted(ex.getResourcePath()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorMessageResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMessage());
        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "none";
        ErrorMessageResponse body = ErrorMessageResponse.of(
                "Method not allowed",
                "Method '%s' is not supported. Supported: %s".formatted(ex.getMethod(), supported));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessageResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body: {}", ex.getMessage());
        ErrorMessageResponse body = ErrorMessageResponse.of(VALIDATION_FAILED, "Malformed or unreadable request body");
        return ResponseEntity.badRequest().body(body);
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
