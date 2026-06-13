package dev.sorokin.eventmanager.repository;

import dev.sorokin.eventmanager.common.exception.EventNotFoundException;
import dev.sorokin.eventmanager.repository.entity.EventEntity;
import dev.sorokin.eventmanager.service.model.EventStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

    default EventEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    @EntityGraph(attributePaths = {"owner", "location"})
    List<EventEntity> findAllByOwnerLogin(String ownerLogin);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            update events
               set status = 'FINISHED'
             where status in ('WAIT_START', 'STARTED')
               and start_at + (duration_minutes * interval '1 minute') <= :now
            """, nativeQuery = true)
    int finishEndedEvents(@Param("now") ZonedDateTime now);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            update events
               set status = 'STARTED'
             where status = 'WAIT_START'
               and start_at <= :now
               and start_at + (duration_minutes * interval '1 minute') > :now
            """, nativeQuery = true)
    int startBegunEvents(@Param("now") ZonedDateTime now);

    @Query("""
            select coalesce(max(e.maxPlaces), 0)
              from EventEntity e
             where e.location.id = :locationId and e.status in :statuses
            """)
    int findMaxRequiredPlaces(@Param("locationId") Long locationId, @Param("statuses") Collection<EventStatus> statuses);

    boolean existsByLocationId(Long locationId);

    @Modifying
    @Query("""
            update EventEntity e
               set e.occupiedPlaces = e.occupiedPlaces + 1
             where e.id = :id and e.occupiedPlaces < e.maxPlaces
            """)
    int reserveSeat(@Param("id") Long id);

    @Modifying
    @Query("""
            update EventEntity e
               set e.occupiedPlaces = e.occupiedPlaces - 1
             where e.id = :id and e.occupiedPlaces > 0
            """)
    void releaseSeat(@Param("id") Long id);
}
