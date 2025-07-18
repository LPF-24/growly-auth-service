package com.example.auth_service.service;

import com.example.auth_service.dto.*;
import com.example.auth_service.entity.Person;
import com.example.auth_service.mapper.PersonConverter;
import com.example.auth_service.mapper.PersonMapper;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.security.PersonDetails;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class PeopleService {
    private final PeopleRepository peopleRepository;
    private final PersonMapper personMapper;
    private final PersonConverter personConverter;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(PeopleService.class);

    public PeopleService(PeopleRepository peopleRepository, PersonMapper personMapper, PersonConverter personConverter, PasswordEncoder passwordEncoder, KafkaTemplate<String, UserDeletedEvent> kafkaTemplate) {
        this.peopleRepository = peopleRepository;
        this.personMapper = personMapper;
        this.personConverter = personConverter;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public PersonResponseDTO savePerson(PersonRequestDTO dto) {
        Person person = personMapper.toEntity(dto);
        peopleRepository.save(person);
        return personMapper.toResponse(person);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') || #personId == authentication.principal.id")
    @Transactional
    public void deletePerson(Long personId) {
        peopleRepository.deleteById(personId);
        kafkaTemplate.send("user-deleted", new UserDeletedEvent(personId));
        logger.info("Sent event to Kafka: userId = {}", personId);
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

        /*boolean allFieldsNull = Stream.of(
                dto.getUsername(),
                dto.getPassword(),
                dto.getEmail()
        ).allMatch(Objects::isNull);

        if (allFieldsNull) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nothing to update");
        }*/

        personConverter.updatePersonFromDtoWithFixedFields(dto, personToUpdate);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            personToUpdate.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return personMapper.toResponse(peopleRepository.save(personToUpdate));
    }

    @Transactional
    public void setLastLogin(Long userId) {
        Person currentUser = peopleRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " wasn't found!"));

        currentUser.setLastLogin(LocalDateTime.now());
        peopleRepository.save(currentUser);
    }
}
