package com.tech.ian.user.exception;

import com.tech.ian.user.exception.exceptions.UserAlreadyEnabledException;
import com.tech.ian.user.exception.exceptions.UserAlreadyExistException;
import com.tech.ian.user.exception.exceptions.UserNotFoundException;
import com.tech.ian.user.exception.exceptions.WrongVerificationCodeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Object> handleUserAlreadyExist(UserAlreadyExistException exception, WebRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.CONFLICT;
        int code = status.value();
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorResponse errorResponse = new ErrorResponse(
                timestamp,
                code,
                status.toString(),
                exception.getMessage(),
                path
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(UserAlreadyEnabledException.class)
    public ResponseEntity<Object> userAlreadyEnabledException(UserAlreadyEnabledException exception, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.CONFLICT;
        int code = status.value();
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorResponse errorResponse = new ErrorResponse(
                timestamp,
                code,
                status.toString(),
                exception.getMessage(),
                path
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(WrongVerificationCodeException.class)
    public ResponseEntity<Object> wrongVerificationCodeException(WrongVerificationCodeException exception, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.CONFLICT;
        int code = status.value();
        String path = request.getContextPath();

        ErrorResponse errorResponse = new ErrorResponse(
                timestamp,
                code,
                status.toString(),
                exception.getMessage(),
                path
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> userNotFoundException(UserNotFoundException exception, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.NOT_FOUND;
        int code = status.value();
        String path = request.getContextPath();
        ErrorResponse errorResponse = new ErrorResponse(timestamp, code, status.toString(), exception.getMessage(), path);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user or password");
    }

    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity<Object> handlerGenericException(BaseAppException exception, WebRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        int code = status.value();
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse(
                timestamp,
                code,
                status.toString(),
                exception.getMessage(),
                path
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}
