package dev.sorokin.eventmanager.service.exception;

import dev.sorokin.eventmanager.common.exception.ItemNotFoundException;
import dev.sorokin.eventmanager.service.model.Location;

public class LocationNotFoundException extends ItemNotFoundException {

    public LocationNotFoundException(Long id) {
        super(Location.class, id);
    }
}
