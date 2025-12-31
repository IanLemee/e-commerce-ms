package com.tech.ian.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<Object> productOutOfStockException(ProductOutOfStockException ex, HttpServletRequest request) {
        HttpStatus noContent = HttpStatus.NO_CONTENT;
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                noContent.value(),
                noContent,
                ex.getMessage(),
                request.getContextPath());
        return new ResponseEntity<>(errorResponse, noContent);
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
                status,
                exception.getMessage(),
                path
        );

        return new ResponseEntity<>(errorResponse, status);
    }

}
