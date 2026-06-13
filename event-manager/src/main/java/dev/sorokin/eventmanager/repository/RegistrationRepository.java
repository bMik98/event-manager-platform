package dev.sorokin.eventmanager.repository;

import dev.sorokin.eventmanager.common.exception.RegistrationNotFoundException;
import dev.sorokin.eventmanager.repository.entity.RegistrationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    Optional<RegistrationEntity> findByEventIdAndUserId(Long eventId, Long userId);

    default RegistrationEntity getByEventIdAndUserIdOrThrow(Long eventId, Long userId) {
        return findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new RegistrationNotFoundException(eventId, userId));
    }

    @EntityGraph(attributePaths = {"event", "event.owner", "event.location"})
    List<RegistrationEntity> findAllByUserLogin(String userLogin);
}
