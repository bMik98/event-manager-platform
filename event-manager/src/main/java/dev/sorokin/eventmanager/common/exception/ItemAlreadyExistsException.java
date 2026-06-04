package dev.sorokin.eventmanager.common.exception;

public class ItemAlreadyExistsException extends RuntimeException {

    public ItemAlreadyExistsException(Class<?> clazz, Object id) {
        this(clazz, "id", id);
    }

    public ItemAlreadyExistsException(Class<?> itemClass, String criteriaName, Object criteriaValue) {
        super("%s with %s '%s' already exists".formatted(itemClass.getSimpleName(), criteriaName, criteriaValue));
    }
}
