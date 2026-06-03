package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.common.EventManagerConstants;
import dev.sorokin.eventmanager.service.model.UserAccount;
import dev.sorokin.eventmanager.service.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Response DTO for {@link UserAccount}
 */
@Schema(description = "User data")
public record UserResponseDto(

        @Schema(description = "Unique user identifier")
        Long id,

        @Schema(description = "Unique user login")
        String login,

        @Schema(description = "User role", implementation = UserRole.class)
        UserRole role,

        @Schema(description = "User age", minimum = "" + EventManagerConstants.MIN_USER_AGE)
        Integer age

) implements Serializable {
}
