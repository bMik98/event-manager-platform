package dev.sorokin.eventmanager.repository.mapper;

import dev.sorokin.eventmanager.repository.entity.EventEntity;
import dev.sorokin.eventmanager.repository.entity.LocationEntity;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.service.model.Event;
import dev.sorokin.eventmanager.service.model.EventCreate;
import dev.sorokin.eventmanager.service.model.EventStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserDbMapper.class, LocationDbMapper.class})
public interface EventDbMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "occupiedPlaces", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "name", source = "command.name")
    EventEntity toEntity(EventCreate command, UserEntity owner, LocationEntity location, EventStatus status);

    Event toDomain(EventEntity entity);

    List<Event> toDomain(List<EventEntity> entityList);
}
