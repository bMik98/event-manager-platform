package dev.sorokin.eventmanager.controller.mapper;

import dev.sorokin.eventmanager.controller.dto.LocationRequestDto;
import dev.sorokin.eventmanager.controller.dto.LocationResponseDto;
import dev.sorokin.eventmanager.service.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationWebMapper {

    LocationResponseDto toDto(Location location);

    List<LocationResponseDto> toDto(List<Location> locationList);

    @Mapping(target = "id", ignore = true)
    Location toDomain(LocationRequestDto dto);
}
