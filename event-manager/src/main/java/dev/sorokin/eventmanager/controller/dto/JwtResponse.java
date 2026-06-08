package dev.sorokin.eventmanager.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token response returned after successful authentication")
public record JwtResponse(

        @Schema(description = "JWT access token")
        String jwtToken
) {
}
