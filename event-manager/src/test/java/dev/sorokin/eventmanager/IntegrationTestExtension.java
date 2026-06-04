package dev.sorokin.eventmanager;

import dev.sorokin.eventmanager.config.DefaultAdminProperties;
import dev.sorokin.eventmanager.controller.dto.AuthenticationRequest;
import dev.sorokin.eventmanager.controller.dto.JwtResponse;
import dev.sorokin.eventmanager.controller.dto.UserRegistrationRequest;
import dev.sorokin.eventmanager.repository.LocationRepository;
import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.service.DefaultAdminInitializer;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

public class IntegrationTestExtension implements BeforeAllCallback, BeforeEachCallback {

    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18-alpine");

    private static DefaultAdminProperties defaultAdminProperties;

    @Override
    public void beforeAll(@NonNull ExtensionContext context) {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
            System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
            System.setProperty("spring.datasource.username", POSTGRES.getUsername());
            System.setProperty("spring.datasource.password", POSTGRES.getPassword());
        }
    }

    @Override
    public void beforeEach(@NonNull ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        defaultAdminProperties = ctx.getBean(DefaultAdminProperties.class);
        ctx.getBean(LocationRepository.class).deleteAll();
        ctx.getBean(UserAccountRepository.class).deleteAll();
        ctx.getBean(DefaultAdminInitializer.class).createDefaultAdmin();
    }

    public static String obtainAdminToken(RestTestClient client) {
        return obtainToken(client, defaultAdminProperties.login(), defaultAdminProperties.password());
    }

    public static void registerUser(RestTestClient client, String login, String password, int age) {
        client.post()
                .uri("/users")
                .body(new UserRegistrationRequest(login, password, age))
                .exchange();
    }

    public static String obtainToken(RestTestClient client, String login, String password) {
        return client.post()
                .uri("/users/auth")
                .body(new AuthenticationRequest(login, password))
                .exchange()
                .expectBody(JwtResponse.class)
                .returnResult()
                .getResponseBody()
                .jwtToken();
    }

    public static String registerAndObtainToken(RestTestClient client, String login, String password, int age) {
        registerUser(client, login, password, age);
        return obtainToken(client, login, password);
    }

    public static RestTestClient.RequestHeadersSpec<?> withBearer(
            RestTestClient.RequestHeadersSpec<?> spec, String token) {
        return spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
