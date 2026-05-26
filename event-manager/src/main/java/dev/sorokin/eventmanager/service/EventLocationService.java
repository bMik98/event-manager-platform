package dev.sorokin.eventmanager.service;

import dev.sorokin.eventmanager.repository.LocationRepository;
import dev.sorokin.eventmanager.repository.entity.LocationEntity;
import dev.sorokin.eventmanager.repository.mapper.LocationDbMapper;
import dev.sorokin.eventmanager.service.exception.LocationNotFoundException;
import dev.sorokin.eventmanager.service.model.Location;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EventLocationService {

    private final LocationRepository locationRepository;
    private final LocationDbMapper mapper;

    @Transactional
    public Location create(Location location) {
        LocationEntity entity = mapper.toEntity(location);
        LocationEntity createdLocation = locationRepository.save(entity);
        return mapper.toDomain(createdLocation);
    }

    @Transactional
    public Location update(Long targetLocationId, Location source) {
        LocationEntity entity = locationRepository.findById(targetLocationId)
                .orElseThrow(() -> new LocationNotFoundException(targetLocationId));
        mapper.updateEntityFromDomain(source, entity);
        LocationEntity updatedEntity = locationRepository.save(entity);
        return mapper.toDomain(updatedEntity);
    }

    public void delete(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new LocationNotFoundException(locationId);
        }
        locationRepository.deleteById(locationId);
    }

    @Transactional
    public Location get(Long id) {
        LocationEntity entity = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
        return mapper.toDomain(entity);
    }

    @Transactional
    public List<Location> getAll() {
        List<LocationEntity> entities = locationRepository.findAll();
        return mapper.toDomain(entities);
    }
}
