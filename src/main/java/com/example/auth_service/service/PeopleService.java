package com.example.auth_service.service;

import com.example.auth_service.dto.PersonRequestDTO;
import com.example.auth_service.entity.Person;
import com.example.auth_service.mapper.PersonMapper;
import com.example.auth_service.repository.PeopleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeopleService {
    private final PeopleRepository peopleRepository;
    private final PersonMapper personMapper;

    @Transactional
    public String savePerson(PersonRequestDTO dto) {
        Person person = personMapper.toEntity(dto);
        peopleRepository.save(person);
        return "Person with username: " + dto.getUsername() + "successfully registered";
    }
}
