package com.example.auth_service.validation.validator;

import com.example.auth_service.dto.PersonRequestDTO;
import com.example.auth_service.dto.PersonUpdateDTO;
import com.example.auth_service.entity.Person;
import com.example.auth_service.repository.PeopleRepository;
import com.example.auth_service.security.PersonDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Optional;

@Component
public class PersonValidator implements Validator {
    private final PeopleRepository peopleRepository;

    public PersonValidator(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return PersonRequestDTO.class.equals(clazz) || PersonUpdateDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        System.out.println("Method validate of PersonValidator started");
        if (target instanceof PersonRequestDTO) {
            validateUsername(((PersonRequestDTO) target).getUsername(), null, errors);
        } else if (target instanceof PersonUpdateDTO dto) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
            validateUsername(dto.getUsername(), personDetails.getId(), errors);
        }
    }

    private void validateUsername(String username, Long id, Errors errors) {
        List<Person> peopleWithSameUsername = peopleRepository.findByUsername(username);

        if (!peopleWithSameUsername.isEmpty()) {
            boolean sameIdExists = peopleWithSameUsername.stream()
                            .anyMatch(person -> person.getId().equals(id));

            if (!sameIdExists) {
                errors.rejectValue("username", "username.taken", "This username is already taken!");
            }
        }
    }
}
