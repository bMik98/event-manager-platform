package dev.sorokin.eventmanager.controller.mapper;

import dev.sorokin.eventmanager.controller.dto.RegistrationResponseDto;
import dev.sorokin.eventmanager.service.model.Registration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegistrationWebMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "userId", source = "userAccount.id")
    RegistrationResponseDto toDto(Registration registration);

    List<RegistrationResponseDto> toDto(List<Registration> registrations);
}
