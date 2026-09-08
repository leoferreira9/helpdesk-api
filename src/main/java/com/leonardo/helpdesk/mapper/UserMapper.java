package com.leonardo.helpdesk.mapper;

import com.leonardo.helpdesk.dto.request.UserRequestDto;
import com.leonardo.helpdesk.dto.response.UserResponseDto;
import com.leonardo.helpdesk.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto convertToResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    User convertToEntity(UserRequestDto requestDto);
}
