package dev.sorokin.eventmanager.service.exception;

import dev.sorokin.eventmanager.common.exception.ItemAlreadyExistsException;
import dev.sorokin.eventmanager.service.model.UserAccount;

public class UserAlreadyExistsException extends ItemAlreadyExistsException {

    public UserAlreadyExistsException(String login) {
        super(UserAccount.class, "login", login);
    }
}
