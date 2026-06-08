package dev.sorokin.eventmanager.config;

import dev.sorokin.eventmanager.controller.dto.AuthenticationRequest;
import dev.sorokin.eventmanager.controller.dto.JwtResponse;
import dev.sorokin.eventmanager.service.DefaultAdminInitializer;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Objects;

public class IntegrationTestExtension implements BeforeEachCallback {

    static final PostgreSQLContainer POSTGRES;
    private static final String TRUNCATE_ALL_TABLES = """
            DO $$
            BEGIN
                EXECUTE (
                    SELECT 'TRUNCATE TABLE ' || string_agg(quote_ident(tablename), ', ') || ' RESTART IDENTITY CASCADE'
                    FROM pg_tables
                    WHERE schemaname = 'public'
                    AND tablename NOT IN ('databasechangelog', 'databasechangeloglock')
                );
            END $$
            """;
    private static DefaultAdminProperties defaultAdminProperties;

    static {
        POSTGRES = new PostgreSQLContainer("postgres:18-alpine")
                .withReuse(true);
        POSTGRES.start();
    }

    public static String obtainAdminToken(RestTestClient client) {
        return obtainToken(client, defaultAdminProperties.login(), defaultAdminProperties.password());
    }

    public static String obtainToken(RestTestClient client, String login, String password) {
        JwtResponse jwtResponse = client.post()
                .uri("/users/auth")
                .body(new AuthenticationRequest(login, password))
                .exchange()
                .expectBody(JwtResponse.class)
                .returnResult()
                .getResponseBody();
        String token = Objects.requireNonNull(jwtResponse).jwtToken();
        return Objects.requireNonNull(token);
    }

    @Override
    public void beforeEach(@NonNull ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        defaultAdminProperties = ctx.getBean(DefaultAdminProperties.class);
        new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class))
                .executeWithoutResult(_ -> ctx.getBean(JdbcTemplate.class).execute(TRUNCATE_ALL_TABLES));
        ctx.getBean(DefaultAdminInitializer.class).createDefaultAdmin();
    }

    public static class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NonNull ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES.getUsername(),
                    "spring.datasource.password=" + POSTGRES.getPassword()
            ).applyTo(context);
        }
    }
}
