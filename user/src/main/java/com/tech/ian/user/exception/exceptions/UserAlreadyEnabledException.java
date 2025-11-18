package com.tech.ian.user.exception.exceptions;

import com.tech.ian.user.exception.BaseAppException;

public class UserAlreadyEnabledException extends BaseAppException {
    public UserAlreadyEnabledException() {
        super("User is already verificated");
    }
}
