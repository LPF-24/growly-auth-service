package com.example.auth_service.repository;

import com.example.auth_service.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PeopleRepository extends JpaRepository<Person, Integer> {
    List<Person> findByUsername(String username);
    List<Person> findByEmail(String email);
    Optional<Person> findFirstByEmail(String email);
    void deleteById(Long personId);
    Optional<Person> findById(Long id);
}
