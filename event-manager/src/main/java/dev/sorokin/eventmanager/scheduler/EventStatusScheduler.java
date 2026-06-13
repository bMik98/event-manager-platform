package dev.sorokin.eventmanager.scheduler;

import dev.sorokin.eventmanager.service.EventStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventStatusService eventStatusService;

    @Scheduled(
            fixedDelayString = "${event.status-scheduler.fixed-delay-ms:60000}",
            initialDelayString = "${event.status-scheduler.initial-delay-ms:10000}"
    )
    public void promoteDueEvents() {
        eventStatusService.promoteDueEvents();
    }
}
