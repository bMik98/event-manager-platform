package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.controller.dto.UserResponseDto;
import dev.sorokin.eventmanager.controller.mapper.UserWebMapper;
import dev.sorokin.eventmanager.service.UserAccountService;
import dev.sorokin.eventmanager.service.model.UserAccount;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserAccountService userAccountService;
    private final UserWebMapper mapper;

    public UserController(UserAccountService userAccountService, UserWebMapper mapper) {
        this.userAccountService = userAccountService;
        this.mapper = mapper;
    }

    @GetMapping("/users")
    public List<UserResponseDto> getUsers() {
        List<UserAccount> userAccounts = userAccountService.getAllUsers();
        return mapper.toDto(userAccounts);
    }

    @GetMapping("/users/{userId}")
    public UserResponseDto getUserById(@Positive @PathVariable Long userId) {
        UserAccount userAccount = userAccountService.getUser(userId);
        return mapper.toDto(userAccount);
    }
}
