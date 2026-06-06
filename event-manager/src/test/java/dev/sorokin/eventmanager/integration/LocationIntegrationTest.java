package dev.sorokin.eventmanager.integration;

import dev.sorokin.eventmanager.config.IntegrationTest;
import dev.sorokin.eventmanager.controller.dto.LocationRequestDto;
import dev.sorokin.eventmanager.controller.dto.LocationResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

import static dev.sorokin.eventmanager.config.IntegrationTestExtension.obtainAdminToken;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class LocationIntegrationTest {

    @Autowired
    RestTestClient restClient;

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
}
