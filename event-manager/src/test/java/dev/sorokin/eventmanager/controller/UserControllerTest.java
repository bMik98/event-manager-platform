package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.ControllerTestConfig;
import dev.sorokin.eventmanager.service.UserAccountService;
import dev.sorokin.eventmanager.common.exception.UserNotFoundException;
import dev.sorokin.eventmanager.service.model.UserAccount;
import dev.sorokin.eventmanager.service.model.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static dev.sorokin.eventmanager.config.SecurityTestUsers.ADMIN;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(ControllerTestConfig.class)
class UserControllerTest {

    private static final UserAccount USER_ACCOUNT =
            new UserAccount(2L, "userctrl", "hash", UserRole.USER, 30);

    @MockitoBean UserAccountService userAccountService;

    @Autowired MockMvc mockMvc;

    @Nested
    class GetAll {

        @Test
        void returns200WithList() throws Exception {
            when(userAccountService.getAllUsers()).thenReturn(List.of(
                    new UserAccount(1L, "admin", "hash", UserRole.ADMIN, 40),
                    USER_ACCOUNT
            ));

            mockMvc.perform(get("/users").with(ADMIN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].login").value("admin"))
                    .andExpect(jsonPath("$[0].role").value("ADMIN"))
                    .andExpect(jsonPath("$[0].age").value(40))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].login").value("userctrl"))
                    .andExpect(jsonPath("$[1].role").value("USER"))
                    .andExpect(jsonPath("$[1].age").value(30))
                    // password hash must never be exposed
                    .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                    .andExpect(jsonPath("$[1].passwordHash").doesNotExist());
        }

        @Test
        void returns200WithEmptyList() throws Exception {
            when(userAccountService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/users").with(ADMIN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class GetById {

        @Test
        void exists_returns200WithCorrectBody() throws Exception {
            when(userAccountService.getUser(2L)).thenReturn(USER_ACCOUNT);

            mockMvc.perform(get("/users/2").with(ADMIN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.login").value("userctrl"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.age").value(30))
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        void notFound_returns404WithErrorResponse() throws Exception {
            when(userAccountService.getUser(999L))
                    .thenThrow(new UserNotFoundException(999L));

            mockMvc.perform(get("/users/999").with(ADMIN))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"))
                    .andExpect(jsonPath("$.detailedMessage").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidIds")
        void invalidId_returns400WithValidationMessage(String reason, long id) throws Exception {
            mockMvc.perform(get("/users/{id}", id).with(ADMIN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        static Stream<Arguments> invalidIds() {
            return Stream.of(
                    arguments("negative", -1L),
                    arguments("zero", 0L)
            );
        }
    }
}
