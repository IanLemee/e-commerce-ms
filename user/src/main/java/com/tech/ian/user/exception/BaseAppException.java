package com.tech.ian.user.exception;

import lombok.Getter;

@Getter
public class BaseAppException extends RuntimeException{

    public BaseAppException(String message) {
        super(message);
    }
}
