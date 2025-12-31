package com.tech.ian.user.exception;

import com.tech.ian.user.exception.exceptions.UserAlreadyEnabledException;
import com.tech.ian.user.exception.exceptions.UserAlreadyExistException;
import com.tech.ian.user.exception.exceptions.UserNotFoundException;
import com.tech.ian.user.exception.exceptions.WrongVerificationCodeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> authenticationException(AuthenticationException ex, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        int code = status.value();
        String path = request.getContextPath();
        ErrorResponse errorResponse = getErrorResponse(ex, timestamp, code, status, path);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> accessDeniedException(AuthenticationException ex, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.FORBIDDEN;
        int code = status.value();
        String path = request.getContextPath();
        ErrorResponse errorResponse = getErrorResponse(ex, timestamp, code, status, path);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Object> handleUserAlreadyExist(UserAlreadyExistException exception, WebRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.CONFLICT;
        int code = status.value();
        String path = request.getContextPath();
        ErrorResponse errorResponse = getErrorResponse(exception, timestamp, code, status, path);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(UserAlreadyEnabledException.class)
    public ResponseEntity<Object> userAlreadyEnabledException(UserAlreadyEnabledException exception, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.CONFLICT;
        int code = status.value();
        String path = request.getContextPath();

        ErrorResponse errorResponse = getErrorResponse(exception, timestamp, code, status, path);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(WrongVerificationCodeException.class)
    public ResponseEntity<Object> wrongVerificationCodeException(WrongVerificationCodeException exception, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.CONFLICT;
        int code = status.value();
        String path = request.getContextPath();

        ErrorResponse errorResponse = getErrorResponse(exception, timestamp, code, status, path);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> userNotFoundException(UserNotFoundException exception, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.NOT_FOUND;
        int code = status.value();
        String path = request.getContextPath();
        ErrorResponse errorResponse = getErrorResponse(exception, timestamp, code, status, path);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        int code = status.value();
        String path = request.getContextPath();
        ErrorResponse errorResponse = getErrorResponse(ex, timestamp, code, status, path);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity<Object> handlerGenericException(BaseAppException exception, WebRequest request) {
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        int code = status.value();
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = getErrorResponse(exception, timestamp, code, status, path);

        return new ResponseEntity<>(errorResponse, status);
    }

    private static ErrorResponse getErrorResponse(Exception exception, Instant timestamp, int code, HttpStatus status, String path) {
        return new ErrorResponse(timestamp, code, status.toString(), exception.getMessage(), path);
    }
}
