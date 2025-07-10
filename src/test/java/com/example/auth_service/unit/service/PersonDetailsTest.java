package com.example.auth_service.unit.service;

import com.example.auth_service.entity.Person;
import com.example.auth_service.security.PersonDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonDetailsTest {

    @Test
    void shouldReturnCorrectAuthorities() {
        Person person = new Person();
        person.setRole("ROLE_USER");

        PersonDetails details = new PersonDetails(person);

        var authorities = details.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void shouldReturnUsernameAndPassword() {
        Person person = new Person();
        person.setUsername("john");
        person.setPassword("secret");

        PersonDetails details = new PersonDetails(person);

        assertEquals("john", details.getUsername());
        assertEquals("secret", details.getPassword());
    }

    @Test
    void shouldReturnAdditionalFields() {
        Person person = new Person();
        person.setId(1L);
        person.setEmail("john@gmail.com");

        PersonDetails details = new PersonDetails(person);

        assertEquals(1L, details.getId());
        assertEquals("john@gmail.com", details.getEmail());
    }
}

