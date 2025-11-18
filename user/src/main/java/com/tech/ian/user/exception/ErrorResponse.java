package com.tech.ian.user.exception;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ErrorResponse {
    private final Instant timestamp;
    private final int code;
    private final String status;
    private final String message;
    private final String path;

    public ErrorResponse(Instant timestamp, int code, String status, String message, String path) {
        this.timestamp = timestamp;
        this.code = code;
        this.status = status;
        this.message = message;
        this.path = path;
    }

}
