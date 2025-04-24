package com.example.auth_service.mapper;

import com.example.auth_service.dto.PersonRequestDTO;
import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.entity.Person;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonMapper {
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public Person toEntity(PersonRequestDTO dto) {
        Person person = new Person();
        person.setUsername(dto.getUsername());
        person.setEmail(dto.getEmail());
        person.setPassword(passwordEncoder.encode(dto.getPassword()));
        person.setRole("ROLE_USER");
        return person;
    }

    public PersonResponseDTO toResponseDTO(Person person) {
        return modelMapper.map(person, PersonResponseDTO.class);
    }
}
