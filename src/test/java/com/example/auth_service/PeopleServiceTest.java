package com.example.auth_service;

import com.example.auth_service.dto.PersonRequestDTO;
import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.entity.Person;
import com.example.auth_service.mapper.PersonMapper;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.service.PeopleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PeopleServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private PersonMapper personMapper;

    @InjectMocks
    private PeopleService peopleService;

    @Test
    void savePerson_shouldSaveAndReturnDTO() {
        // given
        PersonRequestDTO dto = new PersonRequestDTO();
        dto.setUsername("john");
        dto.setEmail("john@example.com");

        Person person = new Person();
        person.setUsername("john");
        person.setEmail("john@example.com");

        Person savedPerson = new Person();
        savedPerson.setId(1L);
        savedPerson.setUsername("john");
        savedPerson.setEmail("john@example.com");
        savedPerson.setRole("ROLE_USER");

        PersonResponseDTO responseDTO = new PersonResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUsername("john");
        responseDTO.setEmail("john@example.com");

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
        Person person = new Person();
        person.setId(1L);
        person.setUsername("john");
        person.setEmail("john@gmail.com");
        person.setRole("ROLE_USER");

        PersonResponseDTO response = new PersonResponseDTO();
        response.setId(1L);
        response.setUsername("john");
        response.setEmail("john@gmail.com");
        response.setRole("ROLE_USER");

        PersonDetails userDetails = new PersonDetails(person);

        // мокаем Authentication и SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

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
}
