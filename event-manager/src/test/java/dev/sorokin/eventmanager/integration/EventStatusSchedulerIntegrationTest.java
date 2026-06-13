package dev.sorokin.eventmanager.integration;

import dev.sorokin.eventmanager.config.IntegrationTest;
import dev.sorokin.eventmanager.repository.EventRepository;
import dev.sorokin.eventmanager.repository.LocationRepository;
import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.repository.entity.EventEntity;
import dev.sorokin.eventmanager.repository.entity.LocationEntity;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.service.EventStatusService;
import dev.sorokin.eventmanager.service.model.EventStatus;
import dev.sorokin.eventmanager.service.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class EventStatusSchedulerIntegrationTest {

    @Autowired
    EventStatusService eventStatusService;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    UserAccountRepository userAccountRepository;
    @Autowired
    LocationRepository locationRepository;

    @Test
    void promoteDueEvents_advancesStatusesByTimeAndLeavesTerminalAndFutureEventsUntouched() {
        UserEntity owner = persistOwner();
        LocationEntity location = persistLocation();
        ZonedDateTime now = ZonedDateTime.now();

        // not yet started — start date is in the future
        long futureWaiting = persistEvent(owner, location, now.plusDays(1), 60, EventStatus.WAIT_START);
        // started but not finished — promoted WAIT_START -> STARTED
        long startedFromWaiting = persistEvent(owner, location, now.minusMinutes(10), 60, EventStatus.WAIT_START);
        // both thresholds passed — jumps WAIT_START -> FINISHED in one pass
        long finishedFromWaiting = persistEvent(owner, location, now.minusMinutes(120), 30, EventStatus.WAIT_START);
        // already STARTED, still running — unchanged
        long stillRunning = persistEvent(owner, location, now.minusMinutes(10), 60, EventStatus.STARTED);
        // already STARTED, end time passed — STARTED -> FINISHED
        long finishedFromStarted = persistEvent(owner, location, now.minusMinutes(120), 30, EventStatus.STARTED);
        // terminal statuses are never touched
        long cancelled = persistEvent(owner, location, now.minusMinutes(120), 30, EventStatus.CANCELLED);
        long finished = persistEvent(owner, location, now.minusMinutes(120), 30, EventStatus.FINISHED);

        int promoted = eventStatusService.promoteDueEvents();

        assertThat(promoted).isEqualTo(3);
        assertThat(statusOf(futureWaiting)).isEqualTo(EventStatus.WAIT_START);
        assertThat(statusOf(startedFromWaiting)).isEqualTo(EventStatus.STARTED);
        assertThat(statusOf(finishedFromWaiting)).isEqualTo(EventStatus.FINISHED);
        assertThat(statusOf(stillRunning)).isEqualTo(EventStatus.STARTED);
        assertThat(statusOf(finishedFromStarted)).isEqualTo(EventStatus.FINISHED);
        assertThat(statusOf(cancelled)).isEqualTo(EventStatus.CANCELLED);
        assertThat(statusOf(finished)).isEqualTo(EventStatus.FINISHED);
    }

    @Test
    void promoteDueEvents_respectsStartDateMovedIntoTheFuture() {
        UserEntity owner = persistOwner();
        LocationEntity location = persistLocation();
        ZonedDateTime now = ZonedDateTime.now();

        // a WAIT_START event whose start was (re)scheduled into the future stays WAIT_START
        long rescheduled = persistEvent(owner, location, now.plusHours(2), 60, EventStatus.WAIT_START);

        assertThat(eventStatusService.promoteDueEvents()).isZero();
        assertThat(statusOf(rescheduled)).isEqualTo(EventStatus.WAIT_START);
    }

    private EventStatus statusOf(long eventId) {
        return eventRepository.findById(eventId).orElseThrow().getStatus();
    }

    private UserEntity persistOwner() {
        UserEntity owner = new UserEntity();
        owner.setLogin("organizer");
        owner.setPasswordHash("hash");
        owner.setRole(UserRole.USER);
        owner.setAge(25);
        return userAccountRepository.save(owner);
    }

    private LocationEntity persistLocation() {
        LocationEntity location = new LocationEntity();
        location.setName("Hall");
        location.setAddress("Some Address");
        location.setCapacity(100);
        return locationRepository.save(location);
    }

    private long persistEvent(UserEntity owner, LocationEntity location,
                              ZonedDateTime startAt, int durationMinutes, EventStatus status) {
        EventEntity event = new EventEntity();
        event.setName("Event");
        event.setOwner(owner);
        event.setLocation(location);
        event.setMaxPlaces(10);
        event.setCost(1000);
        event.setDurationMinutes(durationMinutes);
        event.setStartAt(startAt);
        event.setStatus(status);
        return eventRepository.save(event).getId();
    }
}
