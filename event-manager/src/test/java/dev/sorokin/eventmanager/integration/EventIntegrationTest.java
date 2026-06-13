package dev.sorokin.eventmanager.integration;

import dev.sorokin.eventmanager.config.IntegrationTest;
import dev.sorokin.eventmanager.controller.dto.*;
import dev.sorokin.eventmanager.service.model.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static dev.sorokin.eventmanager.config.IntegrationTestExtension.obtainAdminToken;
import static dev.sorokin.eventmanager.config.IntegrationTestExtension.obtainToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@IntegrationTest
class EventIntegrationTest {

    private static final String PASSWORD = "Password1!";

    @Autowired
    RestTestClient restClient;

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void createAndGetEvent_returnsConsistentData() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        long ownerId = registerUser("organizer");
        String userToken = obtainToken(restClient, "organizer", PASSWORD);

        EventResponseDto created = createEvent(userToken, locationId, "Lecture", 10);

        assertThat(created).isNotNull();
        assertThat(created.id()).isPositive();
        assertThat(created.ownerId()).isEqualTo(ownerId);
        assertThat(created.locationId()).isEqualTo(locationId);
        assertThat(created.status()).isEqualTo(EventStatus.WAIT_START);
        assertThat(created.occupiedPlaces()).isZero();
        assertThat(created.maxPlaces()).isEqualTo(10);

        EventResponseDto fetched = restClient.get().uri("/events/{id}", created.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto.class)
                .returnResult().getResponseBody();

        // Same instant, but Postgres timestamptz stores microseconds while the create response keeps
        // the pre-persist nanoseconds — compare every field, with microsecond tolerance on the date.
        assertThat(fetched).usingRecursiveComparison().ignoringFields("date").isEqualTo(created);
        assertThat(fetched).isNotNull();
        assertThat(fetched.date()).isCloseTo(created.date(), within(1, ChronoUnit.MILLIS));
    }

    @Test
    void getMyEvents_returnsOnlyEventsOwnedByCaller() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);

        long aliceId = registerUser("alice");
        String aliceToken = obtainToken(restClient, "alice", PASSWORD);
        registerUser("bob");
        String bobToken = obtainToken(restClient, "bob", PASSWORD);

        createEvent(aliceToken, locationId, "Alice Lecture 1", 10);
        createEvent(aliceToken, locationId, "Alice Lecture 2", 10);
        createEvent(bobToken, locationId, "Bob Lecture", 10);

        EventResponseDto[] aliceEvents = restClient.get().uri("/events/my")
                .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto[].class)
                .returnResult().getResponseBody();

        assertThat(aliceEvents).hasSize(2);
        assertThat(aliceEvents).allSatisfy(event -> assertThat(event.ownerId()).isEqualTo(aliceId));
    }

    @Test
    void updateEvent_ownerAndAdminAllowed_nonOwnerForbidden() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);

        registerUser("owner");
        String ownerToken = obtainToken(restClient, "owner", PASSWORD);
        registerUser("intruder");
        String intruderToken = obtainToken(restClient, "intruder", PASSWORD);

        EventResponseDto event = createEvent(ownerToken, locationId, "Original", 10);

        // a non-owner USER may not modify someone else's event
        restClient.put().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(intruderToken))
                .body(new EventUpdateRequestDto("Hacked", null, null, null, null, null))
                .exchange()
                .expectStatus().isForbidden();

        // the owner may update
        EventResponseDto updatedByOwner = restClient.put().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                .body(new EventUpdateRequestDto("Renamed by owner", null, null, null, null, null))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto.class)
                .returnResult().getResponseBody();
        assertThat(updatedByOwner).isNotNull();
        assertThat(updatedByOwner.name()).isEqualTo("Renamed by owner");

        // an admin may update any event
        EventResponseDto updatedByAdmin = restClient.put().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .body(new EventUpdateRequestDto("Renamed by admin", null, null, null, null, null))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto.class)
                .returnResult().getResponseBody();
        assertThat(updatedByAdmin).isNotNull();
        assertThat(updatedByAdmin.name()).isEqualTo("Renamed by admin");
    }

    @Test
    void cancelEvent_ownerAndAdminAllowed_nonOwnerForbidden() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);

        registerUser("owner");
        String ownerToken = obtainToken(restClient, "owner", PASSWORD);
        registerUser("intruder");
        String intruderToken = obtainToken(restClient, "intruder", PASSWORD);

        // cancelling is terminal, so use a separate event for each allowed actor
        EventResponseDto ownerEvent = createEvent(ownerToken, locationId, "Owner Event", 10);
        EventResponseDto adminEvent = createEvent(ownerToken, locationId, "Admin Event", 10);

        // a non-owner USER may not cancel someone else's event
        restClient.delete().uri("/events/{id}", ownerEvent.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(intruderToken))
                .exchange()
                .expectStatus().isForbidden();

        // the owner may cancel their own event
        restClient.delete().uri("/events/{id}", ownerEvent.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                .exchange()
                .expectStatus().isNoContent();

        // an admin may cancel any event
        restClient.delete().uri("/events/{id}", adminEvent.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void cancelEvent_movesStatusToCancelled_andBlocksSecondCancel() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        registerUser("organizer");
        String userToken = obtainToken(restClient, "organizer", PASSWORD);

        EventResponseDto event = createEvent(userToken, locationId, "To Cancel", 10);

        restClient.delete().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .exchange()
                .expectStatus().isNoContent();

        // soft delete: the row is still readable, now CANCELLED
        EventResponseDto fetched = restClient.get().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto.class)
                .returnResult().getResponseBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.status()).isEqualTo(EventStatus.CANCELLED);

        // a cancelled event can no longer be cancelled
        restClient.delete().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateEvent_forCancelledEvent_returns400() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        registerUser("organizer");
        String userToken = obtainToken(restClient, "organizer", PASSWORD);

        EventResponseDto event = createEvent(userToken, locationId, "To Cancel", 10);

        restClient.delete().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .exchange()
                .expectStatus().isNoContent();

        restClient.put().uri("/events/{id}", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .body(new EventUpdateRequestDto("Renamed after cancel", null, null, null, null, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createEvent_whenLocationCapacityTooSmall_returns400() {
        String adminToken = obtainAdminToken(restClient);
        long smallLocationId = createLocation(adminToken, 5);
        registerUser("organizer");
        String userToken = obtainToken(restClient, "organizer", PASSWORD);

        var request = new EventCreateRequestDto(
                "Too Big", 10, ZonedDateTime.now().plusDays(10), 1200, 60, smallLocationId);

        restClient.post().uri("/events")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // --- helpers ---

    @Test
    void searchEvents_byExactName_returnsOnlyMatching() {
        String adminToken = obtainAdminToken(restClient);
        long locationId = createLocation(adminToken, 100);
        registerUser("organizer");
        String userToken = obtainToken(restClient, "organizer", PASSWORD);

        createEvent(userToken, locationId, "Lecture", 10);
        createEvent(userToken, locationId, "Workshop", 10);

        var filter = new EventSearchRequestDto(
                "Lecture", null, null, null, null, null, null, null, null, null, null);

        EventResponseDto[] results = restClient.post().uri("/events/search")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .body(filter)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventResponseDto[].class)
                .returnResult().getResponseBody();

        assertThat(results).hasSize(1);
        assertThat(results[0].name()).isEqualTo("Lecture");
    }

    private long registerUser(String login) {
        UserResponseDto user = restClient.post().uri("/users")
                .body(new UserRegistrationRequest(login, PASSWORD, 25))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();
        return Objects.requireNonNull(user).id();
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
}
