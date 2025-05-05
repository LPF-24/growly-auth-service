package com.example.auth_service.security;

import com.example.auth_service.entity.Person;
import com.example.auth_service.repository.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class PersonDetailsService implements UserDetailsService {
    private final PeopleRepository peopleRepository;

    @Autowired
    public PersonDetailsService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person user = peopleRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username doesn't found!"));
        return new PersonDetails(user);
    }
}
