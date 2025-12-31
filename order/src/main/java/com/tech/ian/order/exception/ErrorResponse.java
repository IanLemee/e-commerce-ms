package com.tech.ian.order.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final Instant timestamp;
    private final int code;
    private final String httpStatus;
    private final String message;
    private final String path;
}
