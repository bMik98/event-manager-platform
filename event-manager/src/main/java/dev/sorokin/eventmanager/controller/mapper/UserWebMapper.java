package dev.sorokin.eventmanager.controller.mapper;

import dev.sorokin.eventmanager.controller.dto.UserRegistrationRequest;
import dev.sorokin.eventmanager.controller.dto.UserResponseDto;
import dev.sorokin.eventmanager.service.model.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserWebMapper {

    UserResponseDto toDto(UserAccount userAccount);

    List<UserResponseDto> toDto(List<UserAccount> userAccountList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(dev.sorokin.eventmanager.service.model.UserRole.USER)")
    UserAccount toDomain(UserRegistrationRequest dto, String passwordHash);
}
