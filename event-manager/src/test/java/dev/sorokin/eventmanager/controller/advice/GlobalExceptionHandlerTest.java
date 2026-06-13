package dev.sorokin.eventmanager.controller.advice;

import dev.sorokin.eventmanager.service.exception.ConflictingOperationException;
import dev.sorokin.eventmanager.service.exception.InvalidCommandException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dataIntegrityViolation_mapsToBadRequest() {
        var ex = new DataIntegrityViolationException("duplicate key value violates unique constraint");

        ResponseEntity<ErrorMessageResponse> response = handler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Bad request");
        // the raw DB message must not leak to the client
        assertThat(response.getBody().detailedMessage()).doesNotContain("unique constraint");
    }

    @Test
    void invalidRequest_mapsToBadRequest() {
        var ex = new InvalidCommandException("maxPlaces 1 cannot be lower than the 2 already-registered participants");

        ResponseEntity<ErrorMessageResponse> response = handler.handleInvalidCommandException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Bad request");
        // a business-rule message is safe to surface to the caller
        assertThat(response.getBody().detailedMessage()).contains("already-registered participants");
    }

    @Test
    void conflictingOperation_mapsToBadRequest() {
        var ex = new ConflictingOperationException("Event 42 has no free places");

        ResponseEntity<ErrorMessageResponse> response = handler.handleConflictingOperationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Bad request");
        // a state-conflict message is safe to surface to the caller
        assertThat(response.getBody().detailedMessage()).contains("no free places");
    }

    @Test
    void illegalArgument_mapsToInternalServerError() {
        var ex = new IllegalArgumentException("some broken internal invariant");

        ResponseEntity<ErrorMessageResponse> response = handler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Internal server error");
        // the raw exception message must not leak to the client
        assertThat(response.getBody().detailedMessage()).doesNotContain("broken internal invariant");
    }
}
