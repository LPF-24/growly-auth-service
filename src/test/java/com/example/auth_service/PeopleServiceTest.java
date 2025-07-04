package com.example.auth_service;

import com.example.auth_service.dto.PersonRequestDTO;
import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.dto.PersonUpdateDTO;
import com.example.auth_service.entity.Person;
import com.example.auth_service.mapper.PersonConverter;
import com.example.auth_service.mapper.PersonMapper;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.service.PeopleService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PeopleServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private PersonMapper personMapper;

    @Mock
    private PersonConverter personConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PeopleService peopleService;

    Person person;
    PersonDetails personDetails;

    @Test
    void savePerson_shouldSaveAndReturnDTO() {
        // given
        PersonRequestDTO dto = new PersonRequestDTO();
        dto.setUsername("john");
        dto.setEmail("john@example.com");

        Person person = new Person();
        person.setUsername("john");
        person.setEmail("john@example.com");

        Person savedPerson = createSamplePerson();

        PersonResponseDTO responseDTO = createSampleResponseDTO();

        when(personMapper.toEntity(dto)).thenReturn(person);
        when(peopleRepository.save(person)).thenReturn(savedPerson);
        when(personMapper.toResponse(any(Person.class))).thenReturn(responseDTO);

        // when
        PersonResponseDTO result = peopleService.savePerson(dto);

        // then
        assertEquals(responseDTO.getId(), result.getId());
        assertEquals(responseDTO.getUsername(), result.getUsername());
        assertEquals(responseDTO.getEmail(), result.getEmail());

        verify(personMapper).toEntity(dto);
        verify(peopleRepository).save(person);
        verify(personMapper).toResponse(any(Person.class));
    }

    @Test
    void getCurrentUserInfo_shouldReturnCorrectDTO() {
        // given
        Person person = createSamplePerson();
        PersonResponseDTO response = createSampleResponseDTO();
        PersonDetails userDetails = new PersonDetails(person);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personMapper.toResponse(person)).thenReturn(response);

        // when
        PersonResponseDTO result = peopleService.getCurrentUserInfo();

        // than
        assertEquals("john", result.getUsername());
        verify(peopleRepository).findById(1L);
        verify(personMapper).toResponse(person);
    }

    @Nested
    class UpdatePersonTests {
        @BeforeEach
        void setUp() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            person = createSamplePerson();
            personDetails = new PersonDetails(person);
            when(authentication.getPrincipal()).thenReturn(personDetails);
        }

        @Test
        void updateCurrentUserInfo_shouldReturnCorrectDTO() {
            // given
            person.setPassword("encoded-secret");

            PersonUpdateDTO updateDTO = new PersonUpdateDTO();
            updateDTO.setUsername("john");
            updateDTO.setEmail("john@gmail.com");
            updateDTO.setPassword("secret");

            PersonResponseDTO response = createSampleResponseDTO();

            when(peopleRepository.findById(1L)).thenReturn(Optional.of(person));
            when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

            // when
            when(peopleRepository.save(person)).thenReturn(person);
            when(personMapper.toResponse(person)).thenReturn(response);

            PersonResponseDTO result = peopleService.updateCurrentUserInfo(updateDTO);

            // then
            assertEquals("encoded-secret", person.getPassword());
            assertEquals("john", result.getUsername());
            verify(personConverter).updatePersonFromDtoWithFixedFields(updateDTO, person);
            verify(peopleRepository).findById(1L);
            verify(peopleRepository).save(person);
            verify(personMapper).toResponse(person);
        }

        @Test
        void updateCurrentUserInfo_shouldThrowBadRequestException_whenAllFieldsAreNull() {
            // given
            Person person = new Person();
            person.setId(1L);

            PersonUpdateDTO dto = new PersonUpdateDTO();

            // when
            when(peopleRepository.findById(1L)).thenReturn(Optional.of(person));

            // than
            assertThrows(ResponseStatusException.class, () -> peopleService.updateCurrentUserInfo(dto));
        }

        @Test
        void updateCurrentUserInfo_shouldThrowException_whenUserNotFound() {
            // given
            PersonUpdateDTO dto = new PersonUpdateDTO();
            dto.setUsername("john");

            // when
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // user with this Id wasn't found
            when(peopleRepository.findById(1L)).thenReturn(Optional.empty());

            // then
            assertThrows(EntityNotFoundException.class, () -> peopleService.updateCurrentUserInfo(dto));
        }
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
