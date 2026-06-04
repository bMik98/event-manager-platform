package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.IntegrationTest;
import dev.sorokin.eventmanager.controller.dto.UserRegistrationRequest;
import dev.sorokin.eventmanager.controller.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static dev.sorokin.eventmanager.IntegrationTestExtension.obtainAdminToken;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class UserControllerTest {

    private static final String LOGIN = "userctrl";
    private static final String PASSWORD = "Password1!";
    private static final int AGE = 30;

    @Autowired
    RestTestClient restClient;

    private String token;
    private Long userId;

    @BeforeEach
    void setUp() {
        UserResponseDto registered = restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest(LOGIN, PASSWORD, AGE))
                .exchange()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();

        userId = registered.id();
        token = obtainAdminToken(restClient);
    }

    // ── GET /users ────────────────────────────────────────────────────────────

    @Test
    void getUsers_authenticated_returns200WithList() {
        List<UserResponseDto> body = restClient.get()
                .uri("/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<UserResponseDto>>() {
                })
                .returnResult().getResponseBody();

        assertThat(body).hasSize(2)
                .anySatisfy(user -> {
                    assertThat(user.login()).isEqualTo(LOGIN);
                    assertThat(user.age()).isEqualTo(AGE);
                });
    }

    @Test
    void getUsers_unauthenticated_returns401() {
        restClient.get()
                .uri("/users")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── GET /users/{userId} ───────────────────────────────────────────────────

    @Test
    void getUserById_exists_returns200WithCorrectBody() {
        UserResponseDto body = restClient.get()
                .uri("/users/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo(userId);
        assertThat(body.login()).isEqualTo(LOGIN);
        assertThat(body.age()).isEqualTo(AGE);
    }

    @Test
    void getUserById_notFound_returns404() {
        restClient.get()
                .uri("/users/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getUserById_unauthenticated_returns401() {
        restClient.get()
                .uri("/users/{id}", userId)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getUserById_negativeId_returns400() {
        restClient.get()
                .uri("/users/{id}", -1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
