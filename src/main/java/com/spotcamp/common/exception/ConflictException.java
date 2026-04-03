package com.spotcamp.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's a conflict with current state
 */
public class ConflictException extends BaseException {
    
    public ConflictException(String message) {
        super("CONFLICT_ERROR", HttpStatus.CONFLICT, message);
    }
    
    public ConflictException(String message, Object... args) {
        super("CONFLICT_ERROR", HttpStatus.CONFLICT, message, args);
    }
}