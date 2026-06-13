package dev.sorokin.eventmanager.integration;

import dev.sorokin.eventmanager.config.IntegrationTest;
import dev.sorokin.eventmanager.controller.dto.EventCreateRequestDto;
import dev.sorokin.eventmanager.controller.dto.LocationRequestDto;
import dev.sorokin.eventmanager.controller.dto.LocationResponseDto;
import dev.sorokin.eventmanager.controller.dto.UserRegistrationRequest;
import dev.sorokin.eventmanager.repository.EventRepository;
import dev.sorokin.eventmanager.repository.LocationRepository;
import dev.sorokin.eventmanager.repository.UserAccountRepository;
import dev.sorokin.eventmanager.repository.entity.EventEntity;
import dev.sorokin.eventmanager.repository.entity.LocationEntity;
import dev.sorokin.eventmanager.repository.entity.UserEntity;
import dev.sorokin.eventmanager.service.model.EventStatus;
import dev.sorokin.eventmanager.service.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.ZonedDateTime;

import static dev.sorokin.eventmanager.config.IntegrationTestExtension.obtainAdminToken;
import static dev.sorokin.eventmanager.config.IntegrationTestExtension.obtainToken;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class LocationIntegrationTest {

    private static final String PASSWORD = "Password1!";

    @Autowired
    RestTestClient restClient;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    UserAccountRepository userAccountRepository;

    @Test
    void createAndGetLocation_returnsConsistentData() {
        String token = obtainAdminToken(restClient);
        var request = new LocationRequestDto("Conference Room", "Floor 3", 50, "Main conference room");

        LocationResponseDto created = restClient.post().uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.id()).isPositive();

        LocationResponseDto fetched = restClient.get().uri("/locations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(fetched).isEqualTo(created);
    }

    @Test
    void deleteLocation_subsequentGetReturns404() {
        String token = obtainAdminToken(restClient);
        var request = new LocationRequestDto("Temp Room", "Floor 1", 10, null);

        LocationResponseDto created = restClient.post().uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(request)
                .exchange()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(created).isNotNull();

        restClient.delete().uri("/locations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        restClient.get().uri("/locations/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void reduceCapacityBelowExistingEvent_returns400_butFittingReductionSucceeds() {
        String adminToken = obtainAdminToken(restClient);

        LocationResponseDto location = restClient.post().uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .body(new LocationRequestDto("Big Hall", "Floor 2", 100, null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();
        assertThat(location).isNotNull();

        restClient.post().uri("/users")
                .body(new UserRegistrationRequest("organizer", PASSWORD, 25))
                .exchange()
                .expectStatus().isCreated();
        String userToken = obtainToken(restClient, "organizer", PASSWORD);

        // an upcoming event reserving 80 of the 100 places
        restClient.post().uri("/events")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .body(new EventCreateRequestDto(
                        "Conference", 80, ZonedDateTime.now().plusDays(10), 1200, 60, location.id()))
                .exchange()
                .expectStatus().isCreated();

        // shrinking below the event's 80 places is rejected
        restClient.put().uri("/locations/{id}", location.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .body(new LocationRequestDto("Big Hall", "Floor 2", 50, null))
                .exchange()
                .expectStatus().isBadRequest();

        // shrinking down to exactly the event's 80 places still fits and is allowed
        LocationResponseDto updated = restClient.put().uri("/locations/{id}", location.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .body(new LocationRequestDto("Big Hall", "Floor 2", 80, null))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();
        assertThat(updated).isNotNull();
        assertThat(updated.capacity()).isEqualTo(80);
    }

    @Test
    void deleteLocationWithEvents_returns400() {
        String adminToken = obtainAdminToken(restClient);

        LocationResponseDto location = restClient.post().uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .body(new LocationRequestDto("Occupied Hall", "Floor 4", 100, null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();
        assertThat(location).isNotNull();

        restClient.post().uri("/users")
                .body(new UserRegistrationRequest("planner", PASSWORD, 25))
                .exchange()
                .expectStatus().isCreated();
        String userToken = obtainToken(restClient, "planner", PASSWORD);

        restClient.post().uri("/events")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .body(new EventCreateRequestDto(
                        "Seminar", 10, ZonedDateTime.now().plusDays(10), 1200, 60, location.id()))
                .exchange()
                .expectStatus().isCreated();

        // the location still hosts an event, so it cannot be deleted
        restClient.delete().uri("/locations/{id}", location.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchange()
                .expectStatus().isBadRequest();

        // and it is still there afterwards
        restClient.get().uri("/locations/{id}", location.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void reduceCapacityBelowStartedEvent_returns400() {
        String adminToken = obtainAdminToken(restClient);

        UserEntity owner = persistOwner();
        LocationEntity location = persistLocation(100);
        persistStartedEvent(owner, location, 80);

        restClient.put().uri("/locations/{id}", location.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .body(new LocationRequestDto("Hall", "Some Address", 50, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    private UserEntity persistOwner() {
        UserEntity owner = new UserEntity();
        owner.setLogin("organizer");
        owner.setPasswordHash("hash");
        owner.setRole(UserRole.USER);
        owner.setAge(25);
        return userAccountRepository.save(owner);
    }

    private LocationEntity persistLocation(int capacity) {
        LocationEntity location = new LocationEntity();
        location.setName("Hall");
        location.setAddress("Some Address");
        location.setCapacity(capacity);
        return locationRepository.save(location);
    }

    private void persistStartedEvent(UserEntity owner, LocationEntity location, int maxPlaces) {
        EventEntity event = new EventEntity();
        event.setName("In Progress");
        event.setOwner(owner);
        event.setLocation(location);
        event.setMaxPlaces(maxPlaces);
        event.setCost(1000);
        event.setDurationMinutes(60);
        event.setStartAt(ZonedDateTime.now().minusMinutes(10));
        event.setStatus(EventStatus.STARTED);
        eventRepository.save(event);
    }
}
