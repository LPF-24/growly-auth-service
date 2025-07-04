package com.example.auth_service;

import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.dto.UserStatsDTO;
import com.example.auth_service.entity.Person;
import com.example.auth_service.mapper.PersonMapper;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {
    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    PersonMapper personMapper;

    @InjectMocks
    private AdminService adminService;

    @Nested
    class PromotePersonTests {

        @Test
        void shouldPromotePersonToAdmin() {
            Person person = createSamplePerson();

            when(peopleRepository.findById(1L)).thenReturn(Optional.of(person));

            adminService.promotePerson(1L);

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
        when(personMapper.toResponse(person)).thenReturn(responseDTO);

        List<PersonResponseDTO> result = adminService.findAllUsers();

        assertEquals(1, result.size());
        assertEquals("john", result.get(0).getUsername());

        verify(peopleRepository).findAll();
        verify(personMapper).toResponse(person);
    }

    @Test
    void shouldGetAllUsersStats() {
        Person person = createSamplePerson();
        UserStatsDTO statsDTO = new UserStatsDTO();
        statsDTO.setId(1L);
        statsDTO.setUsername("john");
        statsDTO.setEmail("john@gmail.com");
        statsDTO.setRole("ROLE_USER");
        statsDTO.setLastLogin(LocalDateTime.of(2024, 12, 31, 23, 59));

        when(peopleRepository.findAll()).thenReturn(List.of(person));
        when(personMapper.toStatsDTO(person)).thenReturn(statsDTO);

        List<UserStatsDTO> result = adminService.getAllUserStats();
        assertEquals(1, result.size());
        assertEquals("john", result.get(0).getUsername());

        verify(peopleRepository).findAll();
        verify(personMapper).toStatsDTO(person);
    }

    private static Person createSamplePerson() {
        Person person = new Person();
        person.setId(1L);
        person.setUsername("john");
        person.setEmail("john@gmail.com");
        person.setRole("ROLE_USER");
        return person;
    }

    private static PersonResponseDTO createSampleResponseDTO() {
        PersonResponseDTO response = new PersonResponseDTO();
        response.setId(1L);
        response.setUsername("john");
        response.setEmail("john@gmail.com");
        response.setRole("ROLE_USER");
        return response;
    }
}
