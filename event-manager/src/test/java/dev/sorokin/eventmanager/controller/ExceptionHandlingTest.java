package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.config.PasswordPolicyProperties;
import dev.sorokin.eventmanager.config.ControllerSecurityTestConfig;
import dev.sorokin.eventmanager.controller.mapper.UserWebMapper;
import dev.sorokin.eventmanager.service.UserAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, AuthController.class})
@Import(ControllerSecurityTestConfig.class)
@EnableConfigurationProperties(PasswordPolicyProperties.class)
class ExceptionHandlingTest {

    @MockitoBean
    UserAccountService userAccountService;
    @MockitoBean
    PasswordEncoder passwordEncoder;
    @MockitoBean
    UserWebMapper mapper;

    @Autowired
    MockMvc mockMvc;

    @Test
    void unknownPath_returns404WithErrorResponse() throws Exception {
        mockMvc.perform(get("/swagger-ui/this-page-does-not-exist.html"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"))
                .andExpect(jsonPath("$.detailedMessage").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void wrongHttpMethod_returns405WithErrorResponse() throws Exception {
        mockMvc.perform(delete("/users").with(user("a").roles("ADMIN")))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("Method not allowed"))
                .andExpect(jsonPath("$.detailedMessage").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void malformedBody_returns400WithErrorResponse() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"login":"user","password":"Password1!","age":"not-a-number"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.detailedMessage").value("Malformed or unreadable request body"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
