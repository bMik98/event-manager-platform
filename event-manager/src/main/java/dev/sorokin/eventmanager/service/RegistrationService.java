package dev.sorokin.eventmanager.service;

import dev.sorokin.eventmanager.repository.EventRepository;
import dev.sorokin.eventmanager.repository.RegistrationRepository;
import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.repository.entity.EventEntity;
import dev.sorokin.eventmanager.repository.entity.RegistrationEntity;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.repository.mapper.EventDbMapper;
import dev.sorokin.eventmanager.service.exception.ConflictingOperationException;
import dev.sorokin.eventmanager.service.exception.RegistrationAlreadyExistsException;
import dev.sorokin.eventmanager.service.model.Event;
import dev.sorokin.eventmanager.service.model.EventStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserAccountRepository userAccountRepository;
    private final EventDbMapper eventMapper;

    @Transactional
    public void register(Long eventId, String userLogin) {
        EventEntity event = eventRepository.getByIdOrThrow(eventId);
        if (event.getStatus() != EventStatus.WAIT_START) {
            throw new ConflictingOperationException(
                    "Registration is not allowed for an event in status %s".formatted(event.getStatus())
            );
        }
        // The scheduler that flips WAIT_START -> STARTED runs periodically, so an event whose start time
        // has already passed may still read WAIT_START until the next run. Guard against that gap here.
        if (!event.getStartAt().isAfter(ZonedDateTime.now())) {
            throw new ConflictingOperationException(
                    "Registration is not allowed for event %d whose start time has already passed".formatted(eventId)
            );
        }
        UserEntity user = userAccountRepository.getByLoginOrThrow(userLogin);
        if (event.getOwner().getId().equals(user.getId())) {
            throw new ConflictingOperationException(
                    "Event owner cannot register for their own event %d".formatted(eventId)
            );
        }
        if (registrationRepository.existsByEventIdAndUserId(eventId, user.getId())) {
            throw new RegistrationAlreadyExistsException(eventId, user.getId());
        }
        if (eventRepository.reserveSeat(eventId) == 0) {
            throw new ConflictingOperationException("Event %d has no free places".formatted(eventId));
        }
        RegistrationEntity registration = new RegistrationEntity();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setCreatedAt(ZonedDateTime.now());
        registrationRepository.save(registration);
    }

    @Transactional
    public void cancel(Long eventId, String userLogin) {
        EventEntity event = eventRepository.getByIdOrThrow(eventId);
        EventStatus status = event.getStatus();
        if (status == EventStatus.STARTED || status == EventStatus.FINISHED) {
            throw new ConflictingOperationException(
                    "Registration cannot be cancelled for an event in status %s".formatted(status)
            );
        }
        // Same scheduler gap as in register(): an event past its start time may still read WAIT_START
        // until the next scheduler run, so block cancellation by start time as well as status.
        if (!event.getStartAt().isAfter(ZonedDateTime.now())) {
            throw new ConflictingOperationException(
                    "Registration cannot be cancelled for event %d whose start time has already passed".formatted(eventId)
            );
        }
        UserEntity user = userAccountRepository.getByLoginOrThrow(userLogin);
        RegistrationEntity registration = registrationRepository.getByEventIdAndUserIdOrThrow(eventId, user.getId());
        registrationRepository.delete(registration);
        eventRepository.releaseSeat(eventId);
    }

    @Transactional(readOnly = true)
    public List<Event> getRegisteredEvents(String userLogin) {
        return registrationRepository.findAllByUserLogin(userLogin).stream()
                .map(RegistrationEntity::getEvent)
                .map(eventMapper::toDomain)
                .toList();
    }
}
