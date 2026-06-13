package dev.sorokin.eventmanager.repository;

import dev.sorokin.eventmanager.common.exception.LocationNotFoundException;
import dev.sorokin.eventmanager.repository.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    /** Loads a location by id or raises a 404; the standard lookup for handlers that require the location to exist. */
    default LocationEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new LocationNotFoundException(id));
    }
}
