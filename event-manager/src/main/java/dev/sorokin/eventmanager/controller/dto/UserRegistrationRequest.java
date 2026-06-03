package dev.sorokin.eventmanager.controller.dto;

import dev.sorokin.eventmanager.common.EventManagerConstants;
import dev.sorokin.eventmanager.controller.validation.ValidPassword;
import dev.sorokin.eventmanager.service.model.UserAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * Registration (sign-up) request DTO for {@link UserAccount}
 */
@Schema(description = "Data for user registration")
public record UserRegistrationRequest(

        @Schema(description = "User login. Must be unique")
        @NotBlank @Size(min = 3, max = 255) String login,

        @Schema(description = "User password")
        @ValidPassword String password,

        @Schema(
                description = "User age. Must be " + EventManagerConstants.MIN_USER_AGE + " or older",
                minimum = "" + EventManagerConstants.MIN_USER_AGE
        )
        @NotNull @Min(EventManagerConstants.MIN_USER_AGE) Integer age

) implements Serializable {
}
