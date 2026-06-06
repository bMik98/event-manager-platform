package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.ControllerTestConfig;
import dev.sorokin.eventmanager.config.PasswordPolicyProperties;
import dev.sorokin.eventmanager.security.JwtAuthService;
import dev.sorokin.eventmanager.service.UserAccountService;
import dev.sorokin.eventmanager.service.exception.UserAlreadyExistsException;
import dev.sorokin.eventmanager.service.exception.UserNotFoundException;
import dev.sorokin.eventmanager.service.model.UserAccount;
import dev.sorokin.eventmanager.service.model.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(ControllerTestConfig.class)
@EnableConfigurationProperties(PasswordPolicyProperties.class)
class AuthControllerTest {

    @MockitoBean UserAccountService userAccountService;
    @MockitoBean PasswordEncoder passwordEncoder;

    // Provided as a Mockito mock by SecurityTestConfig — autowired here so it can be stubbed.
    @Autowired JwtAuthService jwtAuthService;

    @Autowired MockMvc mockMvc;

    @Nested
    class Register {

        @Test
        void success_returns201WithUserDto() throws Exception {
            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(userAccountService.createUser(any()))
                    .thenReturn(new UserAccount(1L, "authuser", "hash", UserRole.USER, 25));

            mockMvc.perform(post("/users")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"login":"authuser","password":"Password1!","age":25}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.login").value("authuser"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.age").value(25))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        void duplicateLogin_returns409WithConflictMessage() throws Exception {
            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(userAccountService.createUser(any()))
                    .thenThrow(new UserAlreadyExistsException("authuser"));

            mockMvc.perform(post("/users")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"login":"authuser","password":"Password1!","age":25}
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Conflict"))
                    .andExpect(jsonPath("$.detailedMessage").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        void underageUser_returns400WithValidationMessage() throws Exception {
            mockMvc.perform(post("/users")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"login":"authuser","password":"Password1!","age":17}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidRequests")
        void invalidRequest_returns400(String reason, String json) throws Exception {
            mockMvc.perform(post("/users")
                            .contentType(APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        static Stream<Arguments> invalidRequests() {
            return Stream.of(
                    arguments("blank login", """
                            {"login":"","password":"Password1!","age":25}
                            """),
                    arguments("login too short", """
                            {"login":"ab","password":"Password1!","age":25}
                            """),
                    arguments("no uppercase", """
                            {"login":"user","password":"password1!","age":25}
                            """),
                    arguments("no lowercase", """
                            {"login":"user","password":"PASSWORD1!","age":25}
                            """),
                    arguments("no digit", """
                            {"login":"user","password":"Password!","age":25}
                            """),
                    arguments("no special char", """
                            {"login":"user","password":"Password1","age":25}
                            """),
                    arguments("password too short", """
                            {"login":"user","password":"Pa1!","age":25}
                            """)
            );
        }
    }

    @Nested
    class Authenticate {

        @Test
        void success_returns200WithJwtToken() throws Exception {
            when(userAccountService.getUserByLogin("authuser"))
                    .thenReturn(new UserAccount(1L, "authuser", "hash", UserRole.USER, 25));
            when(passwordEncoder.matches("Password1!", "hash")).thenReturn(true);
            when(jwtAuthService.generateToken("authuser")).thenReturn("test.jwt.token");

            mockMvc.perform(post("/users/auth")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"login":"authuser","password":"Password1!"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jwtToken").value("test.jwt.token"));
        }

        @Test
        void wrongPassword_returns400() throws Exception {
            when(userAccountService.getUserByLogin("authuser"))
                    .thenReturn(new UserAccount(1L, "authuser", "hash", UserRole.USER, 25));
            when(passwordEncoder.matches("WrongPass1!", "hash")).thenReturn(false);

            mockMvc.perform(post("/users/auth")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"login":"authuser","password":"WrongPass1!"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void nonExistentUser_returns404WithNotFoundMessage() throws Exception {
            when(userAccountService.getUserByLogin("nobody"))
                    .thenThrow(new UserNotFoundException("nobody"));

            mockMvc.perform(post("/users/auth")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"login":"nobody","password":"Password1!"}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found"))
                    .andExpect(jsonPath("$.detailedMessage").isNotEmpty());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("blankCredentials")
        void blankCredentials_returns400(String reason, String json) throws Exception {
            mockMvc.perform(post("/users/auth")
                            .contentType(APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> blankCredentials() {
            return Stream.of(
                    arguments("blank login", """
                            {"login":"","password":"Password1!"}
                            """),
                    arguments("blank password", """
                            {"login":"authuser","password":""}
                            """)
            );
        }
    }
}
