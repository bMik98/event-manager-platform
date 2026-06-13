package dev.sorokin.eventmanager.common.exception;

public class UserNotFoundException extends ItemNotFoundException {

    public UserNotFoundException(Long id) {
        super("UserAccount", id);
    }

    public UserNotFoundException(String login) {
        super("UserAccount", "login", login);
    }
}
