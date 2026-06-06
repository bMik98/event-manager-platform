package dev.sorokin.eventmanager.integration;

import dev.sorokin.eventmanager.config.IntegrationTest;
import dev.sorokin.eventmanager.controller.dto.AuthenticationRequest;
import dev.sorokin.eventmanager.controller.dto.JwtResponse;
import dev.sorokin.eventmanager.controller.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class AuthIntegrationTest {

    @Autowired
    RestTestClient restClient;

    @Test
    void registerAndAuthenticate_jwtGrantsAccessToProtectedEndpoint() {
        restClient.post().uri("/users")
                .body(new UserRegistrationRequest("jwtuser", "Password1!", 25))
                .exchange()
                .expectStatus().isCreated();

        JwtResponse jwt = restClient.post().uri("/users/auth")
                .body(new AuthenticationRequest("jwtuser", "Password1!"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtResponse.class)
                .returnResult().getResponseBody();

        assertThat(jwt).isNotNull();
        assertThat(jwt.jwtToken()).isNotBlank();

        restClient.get().uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.jwtToken())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() {
        restClient.get().uri("/locations")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedEndpoint_withInvalidToken_returns401() {
        restClient.get().uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.valid.token")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
