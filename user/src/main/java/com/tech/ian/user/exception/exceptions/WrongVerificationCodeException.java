package com.tech.ian.user.exception.exceptions;

import com.tech.ian.user.exception.BaseAppException;

public class WrongVerificationCodeException extends BaseAppException {
    public WrongVerificationCodeException() {
        super("Wrong verification code");
    }
}
