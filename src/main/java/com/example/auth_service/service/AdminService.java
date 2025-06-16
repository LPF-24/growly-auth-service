package com.example.auth_service.service;

import com.example.auth_service.dto.CodeRequestDTO;
import com.example.auth_service.entity.Person;
import com.example.auth_service.repository.PeopleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final PeopleRepository peopleRepository;

    public AdminService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    public void promotePerson(Long personId) {
        Person user = peopleRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("User with this id " + personId + " can't be found"));

        user.setRole("ROLE_ADMIN");
        peopleRepository.save(user);
    }
}
