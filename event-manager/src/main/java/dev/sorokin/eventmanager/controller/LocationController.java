package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.controller.dto.LocationRequestDto;
import dev.sorokin.eventmanager.controller.dto.LocationResponseDto;
import dev.sorokin.eventmanager.controller.mapper.LocationWebMapper;
import dev.sorokin.eventmanager.service.EventLocationService;
import dev.sorokin.eventmanager.service.model.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LocationController {

    private final EventLocationService locationService;
    private final LocationWebMapper mapper;

    public LocationController(EventLocationService locationService, LocationWebMapper mapper) {
        this.locationService = locationService;
        this.mapper = mapper;
    }

    @GetMapping("/locations")
    public List<LocationResponseDto> locations() {
        List<Location> locations = locationService.getAll();
        return mapper.toDto(locations);
    }

    @GetMapping("/locations/{locationId}")
    public LocationResponseDto location(@Positive @PathVariable Long locationId) {
        Location location = locationService.get(locationId);
        return mapper.toDto(location);
    }

    @PostMapping("/locations")
    @ResponseStatus(code = HttpStatus.CREATED)
    public LocationResponseDto createLocation(@Valid @RequestBody LocationRequestDto dto) {
        Location location = mapper.toDomain(dto);
        Location createdLocation = locationService.create(location);
        return mapper.toDto(createdLocation);
    }

    @PutMapping("/locations/{locationId}")
    public LocationResponseDto updateLocation(
            @Positive @PathVariable Long locationId,
            @Valid @RequestBody LocationRequestDto dto
    ) {
        Location source = mapper.toDomain(dto);
        Location updatedLocation = locationService.update(locationId, source);
        return mapper.toDto(updatedLocation);
    }

    @DeleteMapping("/locations/{locationId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteLocation(@Positive @PathVariable Long locationId) {
        locationService.delete(locationId);
    }
}
