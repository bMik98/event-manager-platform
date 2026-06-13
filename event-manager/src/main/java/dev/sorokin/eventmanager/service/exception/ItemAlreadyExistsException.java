package dev.sorokin.eventmanager.service.exception;

public class ItemAlreadyExistsException extends RuntimeException {

    public ItemAlreadyExistsException(String itemType, Object id) {
        this(itemType, "id", id);
    }

    public ItemAlreadyExistsException(String itemType, String criteriaName, Object criteriaValue) {
        super("%s with %s '%s' already exists".formatted(itemType, criteriaName, criteriaValue));
    }
}
