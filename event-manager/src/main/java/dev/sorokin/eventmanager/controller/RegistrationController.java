package dev.sorokin.eventmanager.controller;

import dev.sorokin.eventmanager.controller.dto.EventResponseDto;
import dev.sorokin.eventmanager.controller.mapper.EventWebMapper;
import dev.sorokin.eventmanager.service.RegistrationService;
import dev.sorokin.eventmanager.service.model.Event;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Registrations", description = "User registration and cancellation for events")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final EventWebMapper eventMapper;

    @PostMapping("/events/registrations/{eventId}")
    public void register(@Positive @PathVariable Long eventId, Authentication authentication) {
        registrationService.register(eventId, authentication.getName());
    }

    @DeleteMapping("/events/registrations/cancel/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@Positive @PathVariable Long eventId, Authentication authentication) {
        registrationService.cancel(eventId, authentication.getName());
    }

    @GetMapping("/events/registrations/my")
    public List<EventResponseDto> getMyRegistrations(Authentication authentication) {
        List<Event> myEvents = registrationService.getRegisteredEvents(authentication.getName());
        return eventMapper.toDto(myEvents);
    }
}
