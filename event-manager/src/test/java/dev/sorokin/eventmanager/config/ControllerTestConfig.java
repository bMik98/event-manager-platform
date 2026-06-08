package dev.sorokin.eventmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Shared configuration for functional {@code @WebMvcTest} controller slices.
 *
 * <p>Bundles the real security setup ({@link ControllerSecurityTestConfig}) with the real web mappers,
 * so functional tests exercise request/response mapping end to end. Security-only slices that
 * mock the mapper should import {@link ControllerSecurityTestConfig} directly instead.
 */
@TestConfiguration
@Import(ControllerSecurityTestConfig.class)
@ComponentScan("dev.sorokin.eventmanager.controller.mapper")
public class ControllerTestConfig {
}
