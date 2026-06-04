package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.IntegrationTest;
import dev.sorokin.eventmanager.controller.advice.ErrorMessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;

import static dev.sorokin.eventmanager.IntegrationTestExtension.obtainAdminToken;
import static dev.sorokin.eventmanager.IntegrationTestExtension.registerAndObtainToken;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class GeneralControllerTest {

    @Autowired
    RestTestClient restClient;

    // ── 404: non-existing path ────────────────────────────────────────────────

    @Test
    void requestToNonExistingPath_returns404WithJsonBody() {
        // Swagger paths are permit-all; a non-existent file triggers NoResourceFoundException
        ErrorMessageResponse body = restClient.get()
                .uri("/swagger-ui/this-page-does-not-exist.html")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessageResponse.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.message()).isEqualTo("Not found");
        assertThat(body.detailedMessage()).isNotBlank();
    }

    // ── 405: wrong HTTP method on a known path ────────────────────────────────

    @Test
    void wrongHttpMethodOnKnownPath_returns405() {
        String token = obtainAdminToken(restClient);
        // /locations/{id} supports GET, PUT, DELETE — POST is not mapped
        restClient.post()
                .uri("/locations/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(Map.of())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
                .expectBody(ErrorMessageResponse.class);
    }

    // ── 400: unreadable / type-mismatched request body ────────────────────────

    @Test
    void unreadableRequestBody_returns400() {
        // age is int in UserRegistrationRequest; sending a string triggers HttpMessageNotReadableException
        restClient.post()
                .uri("/users")
                .body(Map.of("login", "testuser", "password", "Password1!", "age", "not-a-number"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ── 403: authenticated but insufficient role ──────────────────────────────

    @Test
    void userRoleAccessingAdminOnlyEndpoint_returns403() {
        String userToken = registerAndObtainToken(restClient, "regularuser", "Password1!", 25);

        restClient.get()
                .uri("/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isForbidden();
    }
}
