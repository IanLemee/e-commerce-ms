package com.tech.ian.user.exception.exceptions;

import com.tech.ian.user.exception.BaseAppException;

public class UserAlreadyExistException extends BaseAppException {

    public UserAlreadyExistException() {
        super("User with this email already exist");
    }
}
