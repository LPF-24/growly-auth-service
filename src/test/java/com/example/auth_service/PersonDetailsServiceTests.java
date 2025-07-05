package com.example.auth_service;

import com.example.auth_service.entity.Person;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.security.PersonDetails;
import com.example.auth_service.security.PersonDetailsService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonDetailsServiceTests {
    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private PersonDetailsService personDetailsService;

    @Nested
    class loadUserByUsernameTests {

        @Test
        void shouldTakeUsername_andReturnPersonDetails() {
            // given
            Person person = createSamplePerson();
            when(peopleRepository.findByUsername("john")).thenReturn(List.of(person));

            // when
            PersonDetails result = (PersonDetails) personDetailsService.loadUserByUsername("john");

            // then
            assertEquals("john", result.getUsername());
            assertEquals("john@gmail.com", result.getEmail());
            assertEquals("ROLE_USER", result.getAuthorities().iterator().next().getAuthority());

            verify(peopleRepository).findByUsername("john");
        }

        @Test
        void shouldReturnException_whenUserNotFound() {
            when(peopleRepository.findByUsername("john")).thenReturn(Collections.emptyList());
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                    () -> personDetailsService.loadUserByUsername("john"));

            assertEquals("Username doesn't found!", exception.getMessage());
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
}
