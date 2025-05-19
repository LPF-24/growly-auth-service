package com.example.auth_service.mapper;

import com.example.auth_service.dto.PersonRequestDTO;
import com.example.auth_service.dto.PersonUpdateDTO;
import com.example.auth_service.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PersonConverter {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updatePersonFromDtoWithFixedFields(PersonUpdateDTO dto, @MappingTarget Person person);
}
