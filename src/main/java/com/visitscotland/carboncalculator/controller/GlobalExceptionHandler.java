package com.visitscotland.carboncalculator.controller;

import com.visitscotland.carboncalculator.exception.TraceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TraceApiException.class)
    public ResponseEntity<String> handleException(TraceApiException exception){
        if (exception.isApiError()) {
            logger.error(exception.getMessage() + "\n\tCode: {}, message: {}", exception.getStatusCode(), exception.getApiMessage());
        } else {
            logger.error(exception.getMessage(), exception);
        }

        return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
    }
}
