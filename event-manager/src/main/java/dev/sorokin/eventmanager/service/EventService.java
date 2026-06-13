package dev.sorokin.eventmanager.service;

import dev.sorokin.eventmanager.repository.EventRepository;
import dev.sorokin.eventmanager.repository.LocationRepository;
import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.repository.entity.EventEntity;
import dev.sorokin.eventmanager.repository.entity.LocationEntity;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.repository.mapper.EventDbMapper;
import dev.sorokin.eventmanager.repository.specification.EventSpecifications;
import dev.sorokin.eventmanager.service.exception.EventAccessDeniedException;
import dev.sorokin.eventmanager.service.exception.InvalidCommandException;
import dev.sorokin.eventmanager.service.model.*;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserAccountRepository userAccountRepository;
    private final EventDbMapper mapper;

    private static void applyUpdate(EventEntity entity, EventUpdate update, LocationEntity effectiveLocation) {
        if (update.name() != null) {
            entity.setName(update.name());
        }
        if (update.maxPlaces() != null) {
            entity.setMaxPlaces(update.maxPlaces());
        }
        if (update.locationId() != null) {
            entity.setLocation(effectiveLocation);
        }
        if (update.startAt() != null) {
            entity.setStartAt(update.startAt());
        }
        if (update.cost() != null) {
            entity.setCost(update.cost());
        }
        if (update.durationMinutes() != null) {
            entity.setDurationMinutes(update.durationMinutes());
        }
    }

    @Transactional
    public Event createEvent(EventCreate command, String ownerLogin) {
        LocationEntity location = locationRepository.getByIdOrThrow(command.locationId());
        if (command.maxPlaces() > location.getCapacity()) {
            throw new InvalidCommandException(
                    "Location capacity %d cannot host an event with %d places"
                            .formatted(location.getCapacity(), command.maxPlaces())
            );
        }
        UserEntity owner = userAccountRepository.getByLoginOrThrow(ownerLogin);
        EventEntity entity = mapper.toEntity(command, owner, location, EventStatus.WAIT_START);
        return mapper.toDomain(eventRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long eventId) {
        EventEntity entity = eventRepository.getByIdOrThrow(eventId);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public List<Event> getMyEvents(String ownerLogin) {
        List<EventEntity> entities = eventRepository.findAllByOwnerLogin(ownerLogin);
        return mapper.toDomain(entities);
    }

    @Transactional(readOnly = true)
    public List<Event> searchEvents(EventSearchFilter filter) {
        Specification<EventEntity> specification = EventSpecifications.fromFilter(filter);
        List<EventEntity> entities = eventRepository.findAll(specification);
        return mapper.toDomain(entities);
    }

    @Transactional
    public Event updateEvent(Long eventId, EventUpdate update, String currentUserLogin) {
        EventEntity entity = eventRepository.getByIdOrThrow(eventId);
        authorizeModification(entity, currentUserLogin);
        if (entity.getStatus() != EventStatus.WAIT_START) {
            throw new InvalidCommandException(
                    "Event %d cannot be updated in status %s".formatted(eventId, entity.getStatus())
            );
        }
        int effectiveMaxPlaces = update.maxPlaces() != null
                ? update.maxPlaces()
                : entity.getMaxPlaces();
        if (effectiveMaxPlaces < entity.getOccupiedPlaces()) {
            throw new InvalidCommandException(
                    "maxPlaces %d cannot be lower than the %d already-registered participants"
                            .formatted(effectiveMaxPlaces, entity.getOccupiedPlaces())
            );
        }
        LocationEntity effectiveLocation = update.locationId() != null
                ? locationRepository.getByIdOrThrow(update.locationId())
                : entity.getLocation();
        if (effectiveMaxPlaces > effectiveLocation.getCapacity()) {
            throw new InvalidCommandException(
                    "Location capacity %d cannot host an event with %d places"
                            .formatted(effectiveLocation.getCapacity(), effectiveMaxPlaces)
            );
        }
        applyUpdate(entity, update, effectiveLocation);
        EventEntity savedEntity = eventRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Transactional
    public void cancelEvent(Long eventId, String currentUserLogin) {
        EventEntity entity = eventRepository.getByIdOrThrow(eventId);
        authorizeModification(entity, currentUserLogin);
        if (entity.getStatus() != EventStatus.WAIT_START) {
            throw new InvalidCommandException(
                    "Event %d cannot be cancelled in status %s".formatted(eventId, entity.getStatus())
            );
        }
        entity.setStatus(EventStatus.CANCELLED);
        eventRepository.save(entity);
    }

    private void authorizeModification(EventEntity event, String currentUserLogin) {
        UserEntity currentUser = userAccountRepository.getByLoginOrThrow(currentUserLogin);
        boolean isOwner = event.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new EventAccessDeniedException(event.getId());
        }
    }
}
