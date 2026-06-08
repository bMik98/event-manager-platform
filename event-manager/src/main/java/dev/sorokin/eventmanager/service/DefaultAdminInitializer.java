package dev.sorokin.eventmanager.service;

import dev.sorokin.eventmanager.config.DefaultAdminProperties;
import dev.sorokin.eventmanager.service.exception.UserAlreadyExistsException;
import dev.sorokin.eventmanager.service.model.UserAccount;
import dev.sorokin.eventmanager.service.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAdminInitializer {

    private static final int ADMIN_AGE = 30;

    private final UserAccountService userAccountService;
    private final PasswordEncoder passwordEncoder;
    private final DefaultAdminProperties defaultAdminProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultAdmin() {
        if (userAccountService.hasAdminAccount()) {
            log.info("At least one admin account already exists, skipping");
            return;
        }
        UserAccount admin = new UserAccount(
                null,
                defaultAdminProperties.login(),
                passwordEncoder.encode(defaultAdminProperties.password()),
                UserRole.ADMIN,
                ADMIN_AGE
        );
        try {
            userAccountService.createUser(admin);
            log.info("Default admin account '{}' created", defaultAdminProperties.login());
        } catch (UserAlreadyExistsException _) {
            throw new IllegalStateException(
                    "No admin account exists but login '%s' is already taken by a non-admin user. "
                    .formatted(defaultAdminProperties.login()) +
                    "Grant ADMIN role manually or change '%s' in configuration."
                    .formatted(DefaultAdminProperties.LOGIN_PROPERTY)
            );
        }
    }
}
