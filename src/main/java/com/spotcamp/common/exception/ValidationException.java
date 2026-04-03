package com.spotcamp.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for validation errors
 */
public class ValidationException extends BaseException {
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, message);
    }
    
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, 
              String.format("Validation failed for field '%s': %s", field, message));
    }
}