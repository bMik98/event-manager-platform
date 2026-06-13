package dev.sorokin.eventmanager.common.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(String itemType, Object id) {
        this(itemType, "id", id);
    }

    public ItemNotFoundException(String itemType, String criteriaName, Object criteriaValue) {
        super("%s with %s '%s' was not found.".formatted(itemType, criteriaName, criteriaValue));
    }
}
