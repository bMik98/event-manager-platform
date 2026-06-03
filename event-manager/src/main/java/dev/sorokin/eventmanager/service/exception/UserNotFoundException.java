package dev.sorokin.eventmanager.service.exception;

import dev.sorokin.eventmanager.common.exception.ItemNotFoundException;
import dev.sorokin.eventmanager.service.model.UserAccount;

public class UserNotFoundException extends ItemNotFoundException {

    public UserNotFoundException(Long id) {
        super(UserAccount.class, id);
    }

    public UserNotFoundException(String login) {
        super(UserAccount.class, "login", login);
    }
}
