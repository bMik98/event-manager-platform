package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.service.model.UserAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * Authentication (sign-in) request DTO for {@link UserAccount}
 */
@Schema(description = "User credentials for authentication. Used to obtain a JWT token")
public record AuthenticationRequest(

        @Schema(description = "User login for authentication")
        @NotBlank String login,

        @Schema(description = "User password")
        @NotBlank String password

) implements Serializable {
}
