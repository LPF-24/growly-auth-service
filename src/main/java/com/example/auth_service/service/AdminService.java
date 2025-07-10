package com.example.auth_service.service;

import com.example.auth_service.dto.PersonResponseDTO;
import com.example.auth_service.dto.UserStatsDTO;
import com.example.auth_service.entity.Person;
import com.example.auth_service.mapper.PersonMapper;
import com.example.auth_service.repository.PeopleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final PeopleRepository peopleRepository;
    private final PersonMapper personMapper;

    public AdminService(PeopleRepository peopleRepository, PersonMapper personMapper) {
        this.peopleRepository = peopleRepository;
        this.personMapper = personMapper;
    }

    @Transactional
    public void promotePerson(Long personId) {
        Person user = peopleRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("User with this id " + personId + " can't be found"));

        user.setRole("ROLE_ADMIN");
        peopleRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<PersonResponseDTO> findAllUsers() {
        return peopleRepository.findAll().stream()
                .filter(person -> "ROLE_USER".equals(person.getRole()))
                .map(personMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserStatsDTO> getAllUserStats() {
        return peopleRepository.findAll().stream().map(personMapper::toStatsDTO).toList();
    }
}
