package com.tech.ian.user.exception.exceptions;

import com.tech.ian.user.exception.BaseAppException;

public class UserNotFoundException extends BaseAppException {
    public UserNotFoundException() {
        super("User not found");
    }
    public UserNotFoundException(String message) {
        super(message);
    }

}
