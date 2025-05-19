package com.example.auth_service.service;

import com.example.auth_service.dto.PersonRequestDTO;
import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.dto.PersonUpdateDTO;
import com.example.auth_service.entity.Person;
import com.example.auth_service.mapper.PersonConverter;
import com.example.auth_service.mapper.PersonMapper;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.security.PersonDetails;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Stream;

@Service
public class PeopleService {
    private final PeopleRepository peopleRepository;
    private final PersonMapper personMapper;
    private final PersonConverter personConverter;

    public PeopleService(PeopleRepository peopleRepository, PersonMapper personMapper, PersonConverter personConverter) {
        this.peopleRepository = peopleRepository;
        this.personMapper = personMapper;
        this.personConverter = personConverter;
    }

    @Transactional
    public PersonResponseDTO savePerson(PersonRequestDTO dto) {
        Person person = personMapper.toEntity(dto);
        peopleRepository.save(person);
        return personMapper.toResponse(person);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void deletePerson(Long personId) {
        peopleRepository.deleteById(personId);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PersonResponseDTO getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        Person person = peopleRepository.findById(personDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + personDetails.getId() + " wasn't found!"));

        return personMapper.toResponse(person);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public PersonResponseDTO updateCurrentUserInfo(PersonUpdateDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        Person personToUpdate = peopleRepository.findById(personDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + personDetails.getId() + " wasn't found!"));

        boolean allFieldsNull = Stream.of(
                dto.getUsername(),
                dto.getPassword(),
                dto.getEmail()
        ).allMatch(Objects::isNull);

        if (allFieldsNull) {
            throw new BadRequestException("Nothing to update");
        }

        personConverter.updatePersonFromDtoWithFixedFields(dto, personToUpdate);

        return personMapper.toResponse(peopleRepository.save(personToUpdate));
    }
}
