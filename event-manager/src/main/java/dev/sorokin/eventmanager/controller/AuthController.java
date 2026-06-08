package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.security.JwtAuthService;
import dev.sorokin.eventmanager.controller.dto.AuthenticationRequest;
import dev.sorokin.eventmanager.controller.dto.JwtResponse;
import dev.sorokin.eventmanager.controller.dto.UserRegistrationRequest;
import dev.sorokin.eventmanager.controller.dto.UserResponseDto;
import dev.sorokin.eventmanager.controller.mapper.UserWebMapper;
import dev.sorokin.eventmanager.service.UserAccountService;
import dev.sorokin.eventmanager.service.model.UserAccount;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration and authentication with JWT access tokens")
public class AuthController {

    private final UserAccountService userAccountService;
    private final UserWebMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthService jwtAuthService;

    @PostMapping("/users/auth")
    public JwtResponse authenticateUser(@Valid @RequestBody AuthenticationRequest dto) {
        UserAccount userAccount = userAccountService.getUserByLogin(dto.login());
        if (!passwordEncoder.matches(dto.password(), userAccount.passwordHash())) {
            throw new IllegalArgumentException("Invalid login or password");
        }
        String token = jwtAuthService.generateToken(userAccount.login());
        return new JwtResponse(token);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto registerUser(@Valid @RequestBody UserRegistrationRequest dto) {
        String passwordHash = passwordEncoder.encode(dto.password());
        UserAccount userAccount = mapper.toDomain(dto, passwordHash);
        UserAccount createdUserAccount = userAccountService.createUser(userAccount);
        return mapper.toDto(createdUserAccount);
    }
}
