package dev.sorokin.eventmanager.integration;

import dev.sorokin.eventmanager.config.IntegrationTest;
import dev.sorokin.eventmanager.controller.dto.*;
import dev.sorokin.eventmanager.repository.EventRepository;
import dev.sorokin.eventmanager.repository.LocationRepository;
import dev.sorokin.eventmanager.repository.RegistrationRepository;
import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.repository.entity.EventEntity;
import dev.sorokin.eventmanager.repository.entity.LocationEntity;
import dev.sorokin.eventmanager.repository.entity.RegistrationEntity;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.service.model.EventStatus;
import dev.sorokin.eventmanager.service.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.ZonedDateTime;
import java.util.Objects;

import static dev.sorokin.eventmanager.config.IntegrationTestExtension.obtainAdminToken;
import static dev.sorokin.eventmanager.config.IntegrationTestExtension.obtainToken;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class RegistrationIntegrationTest {

    private static final String PASSWORD = "Password1!";

    @Autowired
    RestTestClient restClient;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    UserAccountRepository userAccountRepository;
    @Autowired
    RegistrationRepository registrationRepository;

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void register_incrementsOccupiedPlacesAndAppearsInMyRegistrations() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        String organizerToken = registerUserAndLogin("organizer");
        String participantToken = registerUserAndLogin("participant");

        EventResponseDto event = createEvent(organizerToken, locationId, "Lecture", 10);
        assertThat(event.occupiedPlaces()).isZero();

        register(participantToken, event.id()).expectStatus().isOk();

        // the occupied counter now reflects the single registration
        EventResponseDto afterRegister = getEvent(organizerToken, event.id());
        assertThat(afterRegister.occupiedPlaces()).isEqualTo(1);

        EventResponseDto[] myRegistrations = restClient.get().uri("/events/registrations/my")
                .header(HttpHeaders.AUTHORIZATION, bearer(participantToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto[].class)
                .returnResult().getResponseBody();

        assertThat(myRegistrations).hasSize(1);
        assertThat(myRegistrations[0].id()).isEqualTo(event.id());
    }

    @Test
    void register_twiceForSameEvent_returns400() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        String organizerToken = registerUserAndLogin("organizer");
        String participantToken = registerUserAndLogin("participant");

        EventResponseDto event = createEvent(organizerToken, locationId, "Lecture", 10);

        register(participantToken, event.id()).expectStatus().isOk();
        register(participantToken, event.id()).expectStatus().isEqualTo(400);
    }

    @Test
    void register_byEventOwner_returns400() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        String organizerToken = registerUserAndLogin("organizer");

        EventResponseDto event = createEvent(organizerToken, locationId, "Lecture", 10);

        register(organizerToken, event.id()).expectStatus().isEqualTo(400);
    }

    @Test
    void register_whenEventFull_returns400() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        String organizerToken = registerUserAndLogin("organizer");
        String firstToken = registerUserAndLogin("first");
        String secondToken = registerUserAndLogin("second");

        EventResponseDto event = createEvent(organizerToken, locationId, "Single Seat", 1);

        register(firstToken, event.id()).expectStatus().isOk();
        register(secondToken, event.id()).expectStatus().isEqualTo(400);
    }

    @Test
    void updateEvent_loweringMaxPlacesBelowOccupied_returns400() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        String organizerToken = registerUserAndLogin("organizer");
        String firstToken = registerUserAndLogin("first");
        String secondToken = registerUserAndLogin("second");

        EventResponseDto event = createEvent(organizerToken, locationId, "Lecture", 10);
        register(firstToken, event.id()).expectStatus().isOk();
        register(secondToken, event.id()).expectStatus().isOk();

        // two participants are registered; shrinking capacity below that must be rejected
        restClient.put().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(organizerToken))
                .body(new EventUpdateRequestDto(null, 1, null, null, null, null))
                .exchange()
                .expectStatus().isBadRequest();

        // lowering to exactly the occupied count stays allowed
        EventResponseDto updated = restClient.put().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(organizerToken))
                .body(new EventUpdateRequestDto(null, 2, null, null, null, null))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto.class)
                .returnResult().getResponseBody();
        assertThat(updated).isNotNull();
        assertThat(updated.maxPlaces()).isEqualTo(2);
    }

    @Test
    void cancel_decrementsOccupiedPlacesAndClearsMyRegistrations() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        String organizerToken = registerUserAndLogin("organizer");
        String participantToken = registerUserAndLogin("participant");

        EventResponseDto event = createEvent(organizerToken, locationId, "Lecture", 10);
        register(participantToken, event.id()).expectStatus().isOk();
        assertThat(getEvent(organizerToken, event.id()).occupiedPlaces()).isEqualTo(1);

        restClient.delete().uri("/events/registrations/cancel/{eventId}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(participantToken))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(getEvent(organizerToken, event.id()).occupiedPlaces()).isZero();

        EventResponseDto[] myRegistrations = restClient.get().uri("/events/registrations/my")
                .header(HttpHeaders.AUTHORIZATION, bearer(participantToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto[].class)
                .returnResult().getResponseBody();
        assertThat(myRegistrations).isEmpty();
    }

    @Test
    void register_forMissingEvent_returns404() {
        registerUserAndLogin("participant");
        String participantToken = obtainToken(restClient, "participant", PASSWORD);

        register(participantToken, 9999L).expectStatus().isNotFound();
    }

    @Test
    void cancel_whenNotRegistered_returns404() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        String organizerToken = registerUserAndLogin("organizer");
        String participantToken = registerUserAndLogin("participant");

        EventResponseDto event = createEvent(organizerToken, locationId, "Lecture", 10);

        restClient.delete().uri("/events/registrations/cancel/{eventId}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(participantToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    // --- helpers ---

    @Test
    void register_forCancelledEvent_returns400() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        String organizerToken = registerUserAndLogin("organizer");
        String participantToken = registerUserAndLogin("participant");

        EventResponseDto event = createEvent(organizerToken, locationId, "Lecture", 10);

        // organizer soft-deletes the event -> status CANCELLED
        restClient.delete().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(organizerToken))
                .exchange()
                .expectStatus().isNoContent();

        register(participantToken, event.id()).expectStatus().isEqualTo(400);
    }

    @Test
    void register_forStartedOrFinishedEvent_returns400() {
        String participantToken = registerUserAndLogin("participant");

        // STARTED and FINISHED are only reachable via the scheduler (event creation requires a future
        // date), so seed them straight into the database, then attempt to register over REST.
        UserEntity owner = persistOwner();
        LocationEntity location = persistLocationEntity();
        long startedEvent = persistEvent(owner, location, EventStatus.STARTED);
        long finishedEvent = persistEvent(owner, location, EventStatus.FINISHED);

        register(participantToken, startedEvent).expectStatus().isEqualTo(400);
        register(participantToken, finishedEvent).expectStatus().isEqualTo(400);
    }

    @Test
    void register_whenStartTimePassedButStillWaitStart_returns400() {
        String participantToken = registerUserAndLogin("participant");

        // simulate the gap between startAt passing and the scheduler promoting the event:
        // status is still WAIT_START, but the start time is already in the past.
        UserEntity owner = persistOwner();
        LocationEntity location = persistLocationEntity();
        long alreadyStarted = persistEvent(owner, location, EventStatus.WAIT_START,
                ZonedDateTime.now().minusMinutes(1));

        register(participantToken, alreadyStarted).expectStatus().isEqualTo(400);
    }

    @Test
    void cancel_whenStartTimePassedButStillWaitStart_returns400() {
        String participantToken = registerUserAndLogin("participant");
        UserEntity participant = userAccountRepository.getByLoginOrThrow("participant");

        // an existing registration on a WAIT_START event whose start time has since passed
        // (the scheduler has not promoted it yet) can no longer be cancelled.
        UserEntity owner = persistOwner();
        LocationEntity location = persistLocationEntity();
        long alreadyStarted = persistEvent(owner, location, EventStatus.WAIT_START,
                ZonedDateTime.now().minusMinutes(1));
        persistRegistration(alreadyStarted, participant);

        restClient.delete().uri("/events/registrations/cancel/{eventId}", alreadyStarted)
                .header(HttpHeaders.AUTHORIZATION, bearer(participantToken))
                .exchange()
                .expectStatus().isEqualTo(400);
    }

    private RestTestClient.ResponseSpec register(String token, long eventId) {
        return restClient.post().uri("/events/registrations/{eventId}", eventId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange();
    }

    private String registerUserAndLogin(String login) {
        UserResponseDto user = restClient.post().uri("/users")
                .body(new UserRegistrationRequest(login, PASSWORD, 25))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();
        Objects.requireNonNull(user);
        return obtainToken(restClient, login, PASSWORD);
    }

    private long createLocation(String adminToken, int capacity) {
        LocationResponseDto location = restClient.post().uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .body(new LocationRequestDto("Hall", "Some Address", capacity, null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();
        return Objects.requireNonNull(location).id();
    }

    private EventResponseDto createEvent(String token, long locationId, String name, int maxPlaces) {
        var request = new EventCreateRequestDto(
                name, maxPlaces, ZonedDateTime.now().plusDays(10), 1200, 60, locationId);
        return restClient.post().uri("/events")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EventResponseDto.class)
                .returnResult().getResponseBody();
    }

    private EventResponseDto getEvent(String token, long eventId) {
        return restClient.get().uri("/events/{id}", eventId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto.class)
                .returnResult().getResponseBody();
    }

    private UserEntity persistOwner() {
        UserEntity owner = new UserEntity();
        owner.setLogin("organizer");
        owner.setPasswordHash("hash");
        owner.setRole(UserRole.USER);
        owner.setAge(25);
        return userAccountRepository.save(owner);
    }

    private LocationEntity persistLocationEntity() {
        LocationEntity location = new LocationEntity();
        location.setName("Hall");
        location.setAddress("Some Address");
        location.setCapacity(100);
        return locationRepository.save(location);
    }

    private long persistEvent(UserEntity owner, LocationEntity location, EventStatus status) {
        return persistEvent(owner, location, status, ZonedDateTime.now().minusMinutes(30));
    }

    private long persistEvent(UserEntity owner, LocationEntity location, EventStatus status, ZonedDateTime startAt) {
        EventEntity event = new EventEntity();
        event.setName("Event");
        event.setOwner(owner);
        event.setLocation(location);
        event.setMaxPlaces(10);
        event.setCost(1000);
        event.setDurationMinutes(60);
        event.setStartAt(startAt);
        event.setStatus(status);
        return eventRepository.save(event).getId();
    }

    private void persistRegistration(long eventId, UserEntity user) {
        RegistrationEntity registration = new RegistrationEntity();
        registration.setEvent(eventRepository.findById(eventId).orElseThrow());
        registration.setUser(user);
        registration.setCreatedAt(ZonedDateTime.now());
        registrationRepository.save(registration);
    }
}
