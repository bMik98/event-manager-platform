package dev.sorokin.eventmanager.service;

import dev.sorokin.eventmanager.repository.EventRepository;
import dev.sorokin.eventmanager.repository.LocationRepository;
import dev.sorokin.eventmanager.repository.entity.LocationEntity;
import dev.sorokin.eventmanager.repository.mapper.LocationDbMapper;
import dev.sorokin.eventmanager.common.exception.LocationNotFoundException;
import dev.sorokin.eventmanager.service.exception.InvalidCommandException;
import dev.sorokin.eventmanager.service.model.EventStatus;
import dev.sorokin.eventmanager.service.model.Location;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class LocationService {

    private static final List<EventStatus> ACTIVE_STATUSES =
            List.of(EventStatus.WAIT_START, EventStatus.STARTED);

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final LocationDbMapper mapper;

    @Transactional
    public Location createLocation(Location location) {
        LocationEntity entity = mapper.toEntity(location);
        LocationEntity createdLocation = locationRepository.save(entity);
        return mapper.toDomain(createdLocation);
    }

    @Transactional
    public Location updateLocation(Long targetLocationId, Location source) {
        LocationEntity entity = locationRepository.getByIdOrThrow(targetLocationId);
        if (source.capacity() < entity.getCapacity()) {
            verifyCapacityFitsExistingEvents(targetLocationId, source.capacity());
        }
        mapper.updateEntityFromDomain(source, entity);
        LocationEntity updatedEntity = locationRepository.save(entity);
        return mapper.toDomain(updatedEntity);
    }

    private void verifyCapacityFitsExistingEvents(Long locationId, int newCapacity) {
        int maxRequiredPlaces = eventRepository.findMaxRequiredPlaces(locationId, ACTIVE_STATUSES);
        if (newCapacity < maxRequiredPlaces) {
            throw new InvalidCommandException(
                    "Location capacity cannot be reduced to %d: an upcoming event at this location requires %d places"
                            .formatted(newCapacity, maxRequiredPlaces)
            );
        }
    }

    @Transactional
    public void deleteLocationById(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new LocationNotFoundException(locationId);
        }
        if (eventRepository.existsByLocationId(locationId)) {
            throw new InvalidCommandException(
                    "Location %d cannot be deleted while it still has events".formatted(locationId)
            );
        }
        locationRepository.deleteById(locationId);
    }

    @Transactional(readOnly = true)
    public Location getLocation(Long id) {
        LocationEntity entity = locationRepository.getByIdOrThrow(id);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public List<Location> getAllLocations() {
        List<LocationEntity> entities = locationRepository.findAll();
        return mapper.toDomain(entities);
    }
}
