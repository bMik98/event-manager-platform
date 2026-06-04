package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.IntegrationTest;
import dev.sorokin.eventmanager.controller.dto.LocationRequestDto;
import dev.sorokin.eventmanager.controller.dto.LocationResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static dev.sorokin.eventmanager.IntegrationTestExtension.obtainAdminToken;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class LocationControllerTest {

    private static final LocationRequestDto VALID_REQUEST =
            new LocationRequestDto("Main Hall", "123 Main St", 100, "Main venue");

    @Autowired
    RestTestClient restClient;

    private String token;

    @BeforeEach
    void setUp() {
        token = obtainAdminToken(restClient);
    }

    // ── GET /locations ────────────────────────────────────────────────────────

    @Test
    void getAllLocations_authenticated_returns200WithList() {
        createLocation(VALID_REQUEST);

        List<LocationResponseDto> body = restClient.get()
                .uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<LocationResponseDto>>() {})
                .returnResult().getResponseBody();

        assertThat(body).hasSize(1);
        assertThat(body.get(0).name()).isEqualTo("Main Hall");
    }

    @Test
    void getAllLocations_emptyDatabase_returns200WithEmptyList() {
        List<LocationResponseDto> body = restClient.get()
                .uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<LocationResponseDto>>() {})
                .returnResult().getResponseBody();

        assertThat(body).isEmpty();
    }

    @Test
    void getAllLocations_unauthenticated_returns401() {
        restClient.get()
                .uri("/locations")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── GET /locations/{locationId} ───────────────────────────────────────────

    @Test
    void getLocationById_exists_returns200WithCorrectBody() {
        long id = createLocation(VALID_REQUEST);

        LocationResponseDto body = restClient.get()
                .uri("/locations/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo(id);
        assertThat(body.name()).isEqualTo("Main Hall");
        assertThat(body.address()).isEqualTo("123 Main St");
        assertThat(body.capacity()).isEqualTo(100);
        assertThat(body.description()).isEqualTo("Main venue");
    }

    @Test
    void getLocationById_notFound_returns404() {
        restClient.get()
                .uri("/locations/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getLocationById_unauthenticated_returns401() {
        restClient.get()
                .uri("/locations/{id}", 1)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── POST /locations ───────────────────────────────────────────────────────

    @Test
    void createLocation_success_returns201WithBody() {
        LocationResponseDto body = restClient.post()
                .uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(VALID_REQUEST)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.id()).isPositive();
        assertThat(body.name()).isEqualTo("Main Hall");
        assertThat(body.address()).isEqualTo("123 Main St");
        assertThat(body.capacity()).isEqualTo(100);
        assertThat(body.description()).isEqualTo("Main venue");
    }

    @Test
    void createLocation_withoutDescription_returns201WithNullDescription() {
        LocationResponseDto body = restClient.post()
                .uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new LocationRequestDto("Hall B", "456 Side St", 50, null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(body.description()).isNull();
    }

    @Test
    void createLocation_capacityBelowMinimum_returns400() {
        restClient.post()
                .uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new LocationRequestDto("Tiny Room", "789 Back St", 1, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createLocation_blankName_returns400() {
        restClient.post()
                .uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new LocationRequestDto("", "789 Back St", 50, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createLocation_blankAddress_returns400() {
        restClient.post()
                .uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new LocationRequestDto("Hall C", "", 50, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createLocation_unauthenticated_returns401() {
        restClient.post()
                .uri("/locations")
                .body(VALID_REQUEST)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── PUT /locations/{locationId} ───────────────────────────────────────────

    @Test
    void updateLocation_success_returns200WithUpdatedBody() {
        long id = createLocation(VALID_REQUEST);

        LocationResponseDto body = restClient.put()
                .uri("/locations/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new LocationRequestDto("Updated Hall", "999 New St", 200, "Updated desc"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo(id);
        assertThat(body.name()).isEqualTo("Updated Hall");
        assertThat(body.address()).isEqualTo("999 New St");
        assertThat(body.capacity()).isEqualTo(200);
    }

    @Test
    void updateLocation_notFound_returns404() {
        restClient.put()
                .uri("/locations/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new LocationRequestDto("Hall", "Addr", 50, null))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateLocation_invalidBody_returns400() {
        long id = createLocation(VALID_REQUEST);

        restClient.put()
                .uri("/locations/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new LocationRequestDto("", "Addr", 50, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateLocation_unauthenticated_returns401() {
        restClient.put()
                .uri("/locations/{id}", 1)
                .body(new LocationRequestDto("Hall", "Addr", 50, null))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── DELETE /locations/{locationId} ────────────────────────────────────────

    @Test
    void deleteLocation_success_returns204AndLocationIsGone() {
        long id = createLocation(VALID_REQUEST);

        restClient.delete()
                .uri("/locations/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        restClient.get()
                .uri("/locations/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteLocation_notFound_returns404() {
        restClient.delete()
                .uri("/locations/{id}", 999999)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteLocation_unauthenticated_returns401() {
        restClient.delete()
                .uri("/locations/{id}", 1)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private long createLocation(LocationRequestDto dto) {
        return restClient.post()
                .uri("/locations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(dto)
                .exchange()
                .expectBody(LocationResponseDto.class)
                .returnResult().getResponseBody().id();
    }
}
