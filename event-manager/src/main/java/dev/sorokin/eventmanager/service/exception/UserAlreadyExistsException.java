package dev.sorokin.eventmanager.service.exception;

public class UserAlreadyExistsException extends ItemAlreadyExistsException {

    public UserAlreadyExistsException(String login) {
        super("UserAccount", "login", login);
    }
}
