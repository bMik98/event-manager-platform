package dev.sorokin.eventmanager.service;

import dev.sorokin.eventmanager.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class EventStatusService {

    private final EventRepository eventRepository;

    @Transactional
    public int promoteDueEvents() {
        ZonedDateTime now = ZonedDateTime.now();
        int finished = eventRepository.finishEndedEvents(now);
        int started = eventRepository.startBegunEvents(now);
        int promotedCount = finished + started;
        if (promotedCount > 0) {
            log.info("Event status scheduler promoted {} event(s): {} started, {} finished",
                    promotedCount, started, finished);
        }
        return promotedCount;
    }
}
