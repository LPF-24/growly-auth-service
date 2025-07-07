package com.example.auth_service;

import com.example.auth_service.dto.PersonRequestDTO;
import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.dto.PersonUpdateDTO;
import com.example.auth_service.dto.UserDeletedEvent;
import com.example.auth_service.entity.Person;
import com.example.auth_service.mapper.PersonConverter;
import com.example.auth_service.mapper.PersonConverterImpl;
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
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    /*@Mock
    private PersonMapper personMapper;*/

    // @Mock
    private final PersonConverter personConverter = new PersonConverterImpl();

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;

    @InjectMocks
    private PeopleService peopleService;

    Person person;
    PersonDetails personDetails;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapper();

        PersonMapper personMapper = new PersonMapper(modelMapper, passwordEncoder);
        peopleService = new PeopleService(peopleRepository, personMapper, personConverter, passwordEncoder, kafkaTemplate);
    }

    @Test
    void savePerson_shouldSaveAndReturnDTO() {
        // Arrange
        PersonRequestDTO dto = new PersonRequestDTO();
        dto.setUsername("john");
        dto.setEmail("john@gmail.com");
        dto.setPassword("secret");

        // Мокаем поведение passwordEncoder
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        // Гарантируем, что объект, переданный в save(...), получит id = 1L
        when(peopleRepository.save(any(Person.class))).thenAnswer(invocation -> {
            Person p = invocation.getArgument(0);
            p.setId(1L); // здесь мы симулируем "БД назначила ID"
            return p;
        });

        // Act
        PersonResponseDTO result = peopleService.savePerson(dto);

        // Debug (временно)
        System.out.println("Result DTO ID: " + result.getId());

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("john", result.getUsername());
        assertEquals("john@gmail.com", result.getEmail());

        verify(passwordEncoder).encode("secret");
        verify(peopleRepository).save(any(Person.class));
    }

    @Test
    void modelMapper_shouldMapId() {
        ModelMapper modelMapper = new ModelMapper();
        PersonMapper mapper = new PersonMapper(modelMapper, passwordEncoder);

        Person person = new Person();
        person.setId(1L);
        person.setUsername("john");
        person.setEmail("john@gmail.com");

        PersonResponseDTO dto = mapper.toResponse(person);

        assertEquals(1L, dto.getId()); // если тут упадет — ModelMapper не мапит id
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

        // when
        PersonResponseDTO result = peopleService.getCurrentUserInfo();

        // than
        assertEquals("john", result.getUsername());
        verify(peopleRepository).findById(1L);
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

            when(peopleRepository.findById(1L)).thenReturn(Optional.of(person));
            when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

            // when
            when(peopleRepository.save(person)).thenReturn(person);

            PersonResponseDTO result = peopleService.updateCurrentUserInfo(updateDTO);

            // then
            assertEquals("encoded-secret", person.getPassword());
            assertEquals("john", result.getUsername());
            verify(peopleRepository).findById(1L);
            verify(peopleRepository).save(person);
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

    @Nested
    class SetLastLoginTests {

        @Test
        void checkingThatLastLoginWasAssignedSuccessfully() {
            Person person = createSamplePerson();

            when(peopleRepository.findById(1L)).thenReturn(Optional.of(person));

            peopleService.setLastLogin(1L);

            assertNotNull(person.getLastLogin());
            verify(peopleRepository).save(person);
        }

        @Test
        void shouldThrowException_whenUserNotFound() {
            when(peopleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> peopleService.setLastLogin(99L));
        }
    }

    @Nested
    class DeletePersonTests {

        @Test
        void shouldDeletePerson_andSendKafkaEvent() {
            Long personId = 1L;

            peopleService.deletePerson(personId);

            verify(peopleRepository).deleteById(personId);
            verify(kafkaTemplate).send(eq("user-deleted"),argThat(event ->
                    event != null && event.getPersonId().equals(personId)));
        }
    }

}
