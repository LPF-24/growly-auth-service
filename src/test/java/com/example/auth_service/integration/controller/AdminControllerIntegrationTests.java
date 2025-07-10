package com.example.auth_service.integration.controller;

import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.dto.UserDeletedEvent;
import com.example.auth_service.entity.Person;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.security.JWTUtil;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.service.LogoutService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.springframework.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;


import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminControllerIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private JWTUtil jwtUtil;
    @Autowired private PeopleRepository peopleRepository;

    private static final Long PERSON_ID = 1L;
    private static final String USERNAME = "john";
    private static final String EMAIL = "john@gmail.com";
    
    @MockBean
    private LogoutService logoutService;

    @MockBean
    private KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        when(logoutService.buildLogoutResponse(anyString()))
                .thenReturn(ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, "token=; Max-Age=0")
                        .body(Map.of("message", "Logged out successfully")));
    }

    @AfterEach
    void clearDatabase() {
        peopleRepository.deleteAll();
    }

    @Nested
    class methodPromoteTests {
        @Test
        void promote_shouldChangeRoleAndLogoutSuccessfully() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_USER");
            Long userId = savedPerson.getId();
            String username = savedPerson.getUsername();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_USER");

            mockMvc.perform(patch("/admin/promote")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\": \"400000\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out successfully"))
                    .andExpect(header().exists("Set-Cookie"));
        }

        @Test
        void promote_shouldFailValidation_whenCodeIsEmpty() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_USER");
            Long userId = savedPerson.getId();
            String username = savedPerson.getUsername();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_USER");

            mockMvc.perform(patch("/admin/promote")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\": \"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("code")))
                    .andExpect(jsonPath("$.path").value("/admin/promote"));
        }

        @Test
        void promote_shouldFailValidation_whenRoleIsAdmin() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_ADMIN");
            Long userId = savedPerson.getId();
            String username = savedPerson.getUsername();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_ADMIN");

            mockMvc.perform(patch("/admin/promote")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\": \"400000\"}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.path").value("/admin/promote"));
        }

        @Test
        void promote_shouldFail_whenUserNotFound() throws Exception {
            // Устанавливаем кастомный PersonDetails с несуществующим id
            // Устанавливаем кастомный PersonDetails с несуществующим id
            Person fakeUser = new Person();
            fakeUser.setId(9999L); // несуществующий ID
            fakeUser.setUsername("ghost");
            fakeUser.setPassword("encodedPassword");
            fakeUser.setRole("ROLE_USER");
            PersonDetails fakeUserDetails = new PersonDetails(fakeUser);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(fakeUserDetails, null, fakeUserDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(patch("/admin/promote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\": \"400000\"}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("error: User with this id 9999 can't be found"))
                    .andExpect(jsonPath("$.path").value("/admin/promote"));
        }
    }

    @Nested
    class methodGetAllUsersTests {

        @Test
        void getAllUser_shouldReceiveResponseDTOs() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_ADMIN");
            String username = savedPerson.getUsername();
            Long userId = savedPerson.getId();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_ADMIN");

            mockMvc.perform(get("/admin/all-users")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
                    //.andExpect(jsonPath("$[0].username").value("user4"))
                    //.andExpect(jsonPath("$[0].email").value("user4@example.com"));
        }

        @Test
        void getAllUser_shouldReturnEmptyList_whenNoUsersExist() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_ADMIN");
            String username = savedPerson.getUsername();
            Long userId = savedPerson.getId();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_ADMIN");

            mockMvc.perform(get("/admin/all-users")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void promote_shouldFailValidation_whenCodeIsEmpty() throws Exception {
            mockMvc.perform(get("/admin/all-users"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Unauthorized: missing or invalid token"))
                    .andExpect(jsonPath("$.path").value("/admin/all-users"));
        }

        @Test
        void getAllUser_shouldFailMethod_whenRoleIsUser() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_USER");
            String username = savedPerson.getUsername();
            Long userId = savedPerson.getId();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_USER");

            mockMvc.perform(get("/admin/all-users")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.path").value("/admin/all-users"));
        }

        @Test
        void getAllUser_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
            String token = "Bearer invalid.token.value";

            mockMvc.perform(get("/admin/all-users")
                            .header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    class methodGetUserStatsTests {

        @Test
        void getUserStats_shouldReceiveStatsDTOs() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_ADMIN");
            String username = savedPerson.getUsername();
            Long userId = savedPerson.getId();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_ADMIN");

            mockMvc.perform(get("/admin/stats")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].username").value("user"))
                    .andExpect(jsonPath("$[0].email").value("user@example.com"));
        }

        @Test
        void promote_shouldFailValidation_whenCodeIsEmpty() throws Exception {
            mockMvc.perform(patch("/admin/stats"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Unauthorized: missing or invalid token"))
                    .andExpect(jsonPath("$.path").value("/admin/stats"));
        }

        @Test
        void getAllUser_shouldFailValidation_whenRoleIsUser() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_USER");
            String username = savedPerson.getUsername();
            Long userId = savedPerson.getId();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_USER");

            mockMvc.perform(get("/admin/stats")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.path").value("/admin/stats"));
        }

        @Test
        void getAllUser_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
            String token = "Bearer invalid.token.value";

            mockMvc.perform(get("/admin/stats")
                            .header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    class methodDeleteUserByAdmin {
        @Test
        void deleteUserByAdmin_shouldDeleteUserSuccessfully() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_ADMIN");
            String username = savedPerson.getUsername();
            Long userId = savedPerson.getId();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_ADMIN");

            mockMvc.perform(delete("/admin/delete/" + userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User with ID " + userId + " was deleted by admin"));

            Optional<Person> deleted = peopleRepository.findById(userId);
            assertTrue(deleted.isEmpty());
        }

        @Test
        void deleteUserByAdmin_shouldThrowException_whenUserIsNotAdmin() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_USER");
            String username = savedPerson.getUsername();
            Long userId = savedPerson.getId();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_USER");

            mockMvc.perform(delete("/admin/delete/" + userId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.path").value("/admin/delete/" + userId));
        }

        @Test
        void deleteUserByAdmin_shouldThrowException_whenPathIsNotCorrect() throws Exception {
            Person savedPerson = createSamplePerson("ROLE_ADMIN");
            String username = savedPerson.getUsername();
            Long userId = savedPerson.getId();

            String token = jwtUtil.generateAccessToken(userId, username, "ROLE_ADMIN");

            mockMvc.perform(delete("/admin/delete/abc")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.path").value("/admin/delete/abc"));
        }

        @Test
        void deleteUserByAdmin_shouldSendKafkaEvent() throws Exception {
            Person saved = createSamplePerson("ROLE_ADMIN");
            String username = saved.getUsername();
            Long id = saved.getId();

            String token = jwtUtil.generateAccessToken(id, username, "ROLE_ADMIN");

            mockMvc.perform(delete("/admin/delete/" + id)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User with ID " + id + " was deleted by admin"));

            verify(kafkaTemplate).send(eq("user-deleted"), argThat(event ->
                    event.getPersonId().equals(id)
            ));
        }
    }

    private static PersonResponseDTO createSampleResponseDTO() {
        PersonResponseDTO response = new PersonResponseDTO();
        response.setId(PERSON_ID);
        response.setUsername(USERNAME);
        response.setEmail(EMAIL);
        response.setRole("ROLE_USER");
        return response;
    }

    private Person createSamplePerson(String role) {
        Person person = new Person();
        person.setUsername("user");
        person.setPassword("encodedPassword");
        person.setRole(role);
        person.setEmail("user@example.com");
        return peopleRepository.save(person);
    }
}
