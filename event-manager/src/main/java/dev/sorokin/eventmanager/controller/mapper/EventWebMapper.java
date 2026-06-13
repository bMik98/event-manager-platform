package dev.sorokin.eventmanager.controller.mapper;

import dev.sorokin.eventmanager.controller.dto.EventCreateRequestDto;
import dev.sorokin.eventmanager.controller.dto.EventResponseDto;
import dev.sorokin.eventmanager.controller.dto.EventSearchRequestDto;
import dev.sorokin.eventmanager.controller.dto.EventUpdateRequestDto;
import dev.sorokin.eventmanager.service.model.Event;
import dev.sorokin.eventmanager.service.model.EventCreate;
import dev.sorokin.eventmanager.service.model.EventSearchFilter;
import dev.sorokin.eventmanager.service.model.EventUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventWebMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "date", source = "startAt")
    @Mapping(target = "duration", source = "durationMinutes")
    EventResponseDto toDto(Event event);

    List<EventResponseDto> toDto(List<Event> events);

    @Mapping(target = "startAt", source = "date")
    @Mapping(target = "durationMinutes", source = "duration")
    EventCreate toCreate(EventCreateRequestDto dto);

    @Mapping(target = "startAt", source = "date")
    @Mapping(target = "durationMinutes", source = "duration")
    EventUpdate toUpdate(EventUpdateRequestDto dto);

    EventSearchFilter toFilter(EventSearchRequestDto dto);
}
