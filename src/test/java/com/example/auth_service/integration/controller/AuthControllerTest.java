package com.example.auth_service.integration.controller;

import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.dto.UserDeletedEvent;
import com.example.auth_service.entity.Person;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.security.JWTUtil;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.security.PersonDetailsService;
import com.example.auth_service.security.RefreshTokenService;
import com.example.auth_service.service.PeopleService;
import com.example.auth_service.validation.validator.PersonValidator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PeopleRepository peopleRepository;
    @Autowired private PeopleService peopleService;
    @Autowired private PersonValidator personValidator;
    @Autowired private JWTUtil jwtUtil;

    @MockBean private KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private PersonDetailsService personDetailsService;

    @AfterEach
    void clearDatabase() {
        peopleRepository.deleteAll();
    }

    @Nested
    class performAuthenticationTests {

        @BeforeEach
        void setUp() {
            Person person = createSamplePerson("maria123", "ROLE_USER");
            Person saved = peopleRepository.save(person);

            PersonDetails personDetails = new PersonDetails(saved);

            when(authenticationManager.authenticate(any(Authentication.class)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities()));

            doNothing()
                    .when(refreshTokenService)
                    .saveRefreshToken(eq("maria123"), any());
        }

        @Test
        void performAuthentication_success() throws Exception {
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "maria123",
                                        "password": "password"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("maria123"))
                    .andExpect(jsonPath("$.email").value("maria123@gmail.com"));
        }

        @Test
        void performAuthentication_shouldThrowUnauthorizeException() throws Exception {
            when(authenticationManager.authenticate(any(Authentication.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                        {
                            "username": "user123",
                            "password": "password"
                        }
                        """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Invalid username or password"))
                    .andExpect(jsonPath("$.path").value("/login"));
        }

        @Test
        void performAuthentication_shouldReturn500_whenSaveRefreshTokenFails() throws Exception {
            doThrow(new RuntimeException("Redis unavailable"))
                    .when(refreshTokenService).saveRefreshToken(eq("maria123"), any());

            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "username": "maria123",
                                "password": "password"
                            }
                            """))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Internal server error"));
        }

        @Test
        void performAuthentication_shouldReturn404_whenSetLastLoginFails() throws Exception {
            peopleRepository.deleteAll(); // maria123 исчезает

            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "username": "maria123",
                                "password": "password"
                            }
                            """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("error: User with ID")));
        }
    }

    @Nested
    class registrationTests {
        @Test
        void register_success() throws Exception {
            mockMvc.perform(post("/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                       "username": "maria12",
                                       "password": "Test234!",
                                       "email": "maria12@gmail.com"
                                     }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("maria12"))
                    .andExpect(jsonPath("$.email").value("maria12@gmail.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_USER"));
        }

        @Test
        void register_shouldThrowsValidationException_whenDataIsNotCorrect() throws Exception {
            mockMvc.perform(post("/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                       "username": "m",
                                       "password": "test2",
                                       "email": "maria12gmail.com"
                                     }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.allOf(
                            containsString("username:"),
                            containsString("email:"),
                            containsString("password:"))));
        }

        @Test
        void register_shouldThrowsException_whenUserWithSuchDataIsAlreadyRegistered() throws Exception {
            peopleRepository.save(createSamplePerson("maria12", "ROLE_USER"));

            mockMvc.perform(post("/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                       "username": "maria12",
                                       "password": "Test234!",
                                       "email": "maria12@gmail.com"
                                     }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.allOf(
                            containsString("This username is already taken!"),
                            containsString("This email is already taken!"))));
        }
    }

    @Nested
    class deletePersonTests {
        private String token;
        private PersonDetails personDetails;

        @BeforeEach
        void setUp() {
            Person savedPerson = peopleRepository.save(createSamplePerson("maria12", "ROLE_USER"));
            personDetails = new PersonDetails(savedPerson);
            token = jwtUtil.generateAccessToken(savedPerson.getId(), savedPerson.getUsername(), "ROLE_USER");
        }

        @Test
        void deletePerson_success() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(delete("/delete")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User's account with id " + personDetails.getId() + " successfully deleted."));

            Optional<Person> deleted = peopleRepository.findById(personDetails.getId());
            assertTrue(deleted.isEmpty());

            verify(kafkaTemplate).send(eq("user-deleted"), argThat(event -> event.getPersonId().equals(personDetails.getId())));
        }

        @Test
        void deletePerson_shouldReturn401_whenTokenIsMissing() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(delete("/delete"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Unauthorized: missing or invalid token"));
        }

        @Test
        void deletePerson_shouldReturn403_whenUserIsNotOwner() throws Exception {
            token = jwtUtil.generateAccessToken(999L, "maria12", "ROLE_USER");
            mockMvc.perform(delete("/delete")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal server error"));
        }
    }

    @Nested
    class getProfileInfoTests {
        private PersonDetails personDetails;
        private String token;

        @BeforeEach
        void setUp() {
            Person savedPerson = peopleRepository.save(createSamplePerson("maria12", "ROLE_USER"));
            personDetails = new PersonDetails(savedPerson);
            token = jwtUtil.generateAccessToken(savedPerson.getId(), savedPerson.getUsername(), "ROLE_USER");
        }

        @Test
        void getProfileInfo_success() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/profile")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("maria12"))
                    .andExpect(jsonPath("$.email").value("maria12@gmail.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_USER"));
        }

        @Test
        void getProfileInfo_shouldThrows401Exception_whenTokenIsMissing() throws Exception {
            mockMvc.perform(get("/profile"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Unauthorized: missing or invalid token"))
                    .andExpect(jsonPath("$.path").value("/profile"));
        }
    }

    private Person createSamplePerson(String username, String role) {
        Person person = new Person();
        person.setUsername(username);
        person.setPassword("encodedPassword");
        person.setRole(role);
        person.setEmail(username + "@gmail.com");
        return person;
    }

    private PersonResponseDTO createSampleResponseDTO(Long id, String username, String email, String role) {
        PersonResponseDTO response = new PersonResponseDTO();
        response.setId(id);
        response.setUsername(username);
        response.setEmail(email);
        response.setRole(role);
        return response;
    }
}