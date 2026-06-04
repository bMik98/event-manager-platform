package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.IntegrationTest;
import dev.sorokin.eventmanager.controller.dto.AuthenticationRequest;
import dev.sorokin.eventmanager.controller.dto.JwtResponse;
import dev.sorokin.eventmanager.controller.dto.UserRegistrationRequest;
import dev.sorokin.eventmanager.controller.dto.UserResponseDto;
import dev.sorokin.eventmanager.service.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import static dev.sorokin.eventmanager.IntegrationTestExtension.registerUser;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class AuthControllerTest {

    private static final String LOGIN = "authuser";
    private static final String PASSWORD = "Password1!";
    private static final int AGE = 25;

    @Autowired
    RestTestClient restClient;

    // ── POST /users (register) ────────────────────────────────────────────────

    @Test
    void registerUser_success_returns201WithUserDto() {
        UserResponseDto body = restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest(LOGIN, PASSWORD, AGE))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.id()).isPositive();
        assertThat(body.login()).isEqualTo(LOGIN);
        assertThat(body.age()).isEqualTo(AGE);
        assertThat(body.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void registerUser_duplicateLogin_returns400() {
        registerUser(restClient, LOGIN, PASSWORD, AGE);

        restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest(LOGIN, PASSWORD, AGE))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void registerUser_passwordWithoutUppercase_returns400() {
        restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest(LOGIN, "password1!", AGE))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void registerUser_passwordWithoutDigit_returns400() {
        restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest(LOGIN, "Password!", AGE))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void registerUser_passwordWithoutSpecialChar_returns400() {
        restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest(LOGIN, "Password1", AGE))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void registerUser_passwordTooShort_returns400() {
        restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest(LOGIN, "Pa1!", AGE))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void registerUser_underageUser_returns400() {
        restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest(LOGIN, PASSWORD, 17))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void registerUser_blankLogin_returns400() {
        restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest("", PASSWORD, AGE))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void registerUser_loginTooShort_returns400() {
        restClient.post()
                .uri("/users")
                .body(new UserRegistrationRequest("ab", PASSWORD, AGE))
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ── POST /users/auth (authenticate) ──────────────────────────────────────

    @Test
    void authenticateUser_success_returnsJwtToken() {
        registerUser(restClient, LOGIN, PASSWORD, AGE);

        JwtResponse body = restClient.post()
                .uri("/users/auth")
                .body(new AuthenticationRequest(LOGIN, PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtResponse.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.jwtToken()).isNotBlank();
    }

    @Test
    void authenticateUser_wrongPassword_returns400() {
        registerUser(restClient, LOGIN, PASSWORD, AGE);

        restClient.post()
                .uri("/users/auth")
                .body(new AuthenticationRequest(LOGIN, "WrongPass1!"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void authenticateUser_nonExistentUser_returns404() {
        restClient.post()
                .uri("/users/auth")
                .body(new AuthenticationRequest("nobody", PASSWORD))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void authenticateUser_blankLogin_returns400() {
        restClient.post()
                .uri("/users/auth")
                .body(new AuthenticationRequest("", PASSWORD))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void authenticateUser_blankPassword_returns400() {
        restClient.post()
                .uri("/users/auth")
                .body(new AuthenticationRequest(LOGIN, ""))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
