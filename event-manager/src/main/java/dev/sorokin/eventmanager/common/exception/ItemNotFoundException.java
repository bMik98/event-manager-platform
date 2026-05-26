package dev.sorokin.eventmanager.common.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Class<?> clazz, Object id) {
        this(clazz, "id", id);
    }

    public ItemNotFoundException(Class<?> itemClass, String criteriaName, Object criteriaValue) {
        super("%s with %s '%s' was not found.".formatted(itemClass.getSimpleName(), criteriaName, criteriaValue));
    }
}
