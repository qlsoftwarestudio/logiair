package com.logiair.os.user.mapper;

import com.logiair.os.models.User;
import com.logiair.os.user.dto.UserRequestDTO;
import com.logiair.os.user.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    User toEntity(UserRequestDTO dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "lastname", source = "lastname")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    UserResponseDTO toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    void updateUserFromDto(UserRequestDTO dto, @MappingTarget User user);

}
