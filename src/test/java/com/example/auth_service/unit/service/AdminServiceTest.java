package com.example.auth_service.unit.service;

import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.dto.UserStatsDTO;
import com.example.auth_service.entity.Person;
import com.example.auth_service.mapper.PersonMapper;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    private static final Long PERSON_ID = 1L;
    private static final String USERNAME = "john";
    private static final String EMAIL = "john@gmail.com";

    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapper();

        // если не нужен реальный хеш, можно подставить dummy PasswordEncoder
        PasswordEncoder passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "encoded:" + rawPassword;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals("encoded:" + rawPassword);
            }
        };

        PersonMapper personMapper = new PersonMapper(modelMapper, passwordEncoder);
        adminService = new AdminService(peopleRepository, personMapper);
    }

    @Nested
    class PromotePersonTests {

        @Test
        void shouldPromotePersonToAdmin() {
            Person person = createSamplePerson();

            when(peopleRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));

            adminService.promotePerson(PERSON_ID);

            verify(peopleRepository).save(person);
            assertEquals("ROLE_ADMIN", person.getRole());
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            Long userId = 99L;

            when(peopleRepository.findById(userId)).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class, () -> adminService.promotePerson(userId));
        }
    }

    @Test
    void shouldGetAllUsersInfo() {
        Person person = createSamplePerson();
        PersonResponseDTO responseDTO = createSampleResponseDTO();

        when(peopleRepository.findAll()).thenReturn(List.of(person));

        List<PersonResponseDTO> result = adminService.findAllUsers();

        assertEquals(responseDTO.getEmail(), result.get(0).getEmail());
        assertEquals(responseDTO.getId(), result.get(0).getId());
        assertEquals(EMAIL, result.get(0).getEmail());
        assertEquals("ROLE_USER", result.get(0).getRole());
        assertEquals(PERSON_ID, result.get(0).getId());
        assertEquals(1, result.size());
        assertEquals(USERNAME, result.get(0).getUsername());

        verify(peopleRepository).findAll();
    }

    @Test
    void shouldGetAllUsersStats() {
        Person person = createSamplePerson();
        UserStatsDTO statsDTO = new UserStatsDTO();
        statsDTO.setId(PERSON_ID);
        statsDTO.setUsername(USERNAME);
        statsDTO.setEmail(EMAIL);
        statsDTO.setRole("ROLE_USER");
        statsDTO.setLastLogin(LocalDateTime.of(2024, 12, 31, 23, 59));

        when(peopleRepository.findAll()).thenReturn(List.of(person));

        List<UserStatsDTO> result = adminService.getAllUserStats();

        assertEquals(statsDTO.getUsername(), result.get(0).getUsername());
        assertEquals(statsDTO.getRole(), result.get(0).getRole());
        assertEquals(EMAIL, result.get(0).getEmail());
        assertEquals("ROLE_USER", result.get(0).getRole());
        assertEquals(PERSON_ID, result.get(0).getId());
        assertEquals(1, result.size());
        assertEquals(USERNAME, result.get(0).getUsername());

        verify(peopleRepository).findAll();
    }

    @Test
    void shouldReturnEmptyList_whenNoUsers() {
        when(peopleRepository.findAll()).thenReturn(List.of());

        List<PersonResponseDTO> result1 = adminService.findAllUsers();
        List<UserStatsDTO> result2 = adminService.getAllUserStats();

        assertEquals(0, result1.size());
        assertEquals(0, result2.size());

        verify(peopleRepository, times(2)).findAll();
    }

    private static Person createSamplePerson() {
        Person person = new Person();
        person.setId(PERSON_ID);
        person.setUsername(USERNAME);
        person.setEmail(EMAIL);
        person.setRole("ROLE_USER");
        return person;
    }

    private static PersonResponseDTO createSampleResponseDTO() {
        PersonResponseDTO response = new PersonResponseDTO();
        response.setId(PERSON_ID);
        response.setUsername(USERNAME);
        response.setEmail(EMAIL);
        response.setRole("ROLE_USER");
        return response;
    }
}
