package com.example.auth_service.integration.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
public class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldHandleTypeMismatchException() throws Exception {
        mockMvc.perform(delete("/test-errors/mismatch/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value 'abc' for parameter 'id'"))
                .andExpect(jsonPath("$.path").value("/test-errors/mismatch/abc"));
    }

    @Test
    void shouldHandleEntityNotFoundException() throws Exception {
        mockMvc.perform(get("/test-errors/entity-not-found"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("error: User not found"))
                .andExpect(jsonPath("$.path").value("/test-errors/entity-not-found"));
    }

    @Test
    void shouldHandleConstraintViolationException() throws Exception {
        mockMvc.perform(get("/test-errors/constraint/2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                // ожидается, что поле message будет содержать подстроку Email should be valid
                .andExpect(jsonPath("$.message").value(containsString("id: must be greater than or equal to 5")))
                .andExpect(jsonPath("$.path").value("/test-errors/constraint/2"));
    }

    @Test
    void shouldHandleAnyUncheckedException() throws Exception {
        mockMvc.perform(get("/test-errors/runtime-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }

    @Test
    void shouldHandleValidationException_whenInvalidInput() throws Exception {
        String invalidJson = """
                {
                    "username": "",
                    "password": "",
                    "email": "invalid-email"
                }
                """;

        mockMvc.perform(post("/registration")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Username can't be empty")))
                .andExpect(jsonPath("$.message", containsString("Password can't be empty")))
                .andExpect(jsonPath("$.message", containsString("Email should be valid")))
                .andExpect(jsonPath("$.path").value("/registration"));
    }

}
