package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.controller.dto.EventCreateRequestDto;
import dev.sorokin.eventmanager.controller.dto.EventResponseDto;
import dev.sorokin.eventmanager.controller.dto.EventSearchRequestDto;
import dev.sorokin.eventmanager.controller.dto.EventUpdateRequestDto;
import dev.sorokin.eventmanager.controller.mapper.EventWebMapper;
import dev.sorokin.eventmanager.service.EventService;
import dev.sorokin.eventmanager.service.model.Event;
import dev.sorokin.eventmanager.service.model.EventCreate;
import dev.sorokin.eventmanager.service.model.EventSearchFilter;
import dev.sorokin.eventmanager.service.model.EventUpdate;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event CRUD, search and ownership rules")
public class EventController {

    private final EventService eventService;
    private final EventWebMapper mapper;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponseDto createEvent(@Valid @RequestBody EventCreateRequestDto dto, Authentication authentication) {
        EventCreate create = mapper.toCreate(dto);
        Event event = eventService.createEvent(create, authentication.getName());
        return mapper.toDto(event);
    }

    @GetMapping("/events/{eventId}")
    public EventResponseDto getEvent(@Positive @PathVariable Long eventId) {
        Event event = eventService.getEvent(eventId);
        return mapper.toDto(event);
    }

    @PutMapping("/events/{eventId}")
    public EventResponseDto updateEvent(
            @Positive @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequestDto dto,
            Authentication authentication
    ) {
        EventUpdate update = mapper.toUpdate(dto);
        Event event = eventService.updateEvent(eventId, update, authentication.getName());
        return mapper.toDto(event);
    }

    @DeleteMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@Positive @PathVariable Long eventId, Authentication authentication) {
        eventService.cancelEvent(eventId, authentication.getName());
    }

    @PostMapping("/events/search")
    public List<EventResponseDto> searchEvents(@RequestBody EventSearchRequestDto dto) {
        EventSearchFilter filter = mapper.toFilter(dto);
        List<Event> events = eventService.searchEvents(filter);
        return mapper.toDto(events);
    }

    @GetMapping("/events/my")
    public List<EventResponseDto> getMyEvents(Authentication authentication) {
        List<Event> events = eventService.getMyEvents(authentication.getName());
        return mapper.toDto(events);
    }
}
