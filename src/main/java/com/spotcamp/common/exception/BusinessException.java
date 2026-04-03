package com.spotcamp.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for business logic violations
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String message) {
        super("BUSINESS_ERROR", HttpStatus.BAD_REQUEST, message);
    }
    
    public BusinessException(String message, Object... args) {
        super("BUSINESS_ERROR", HttpStatus.BAD_REQUEST, message, args);
    }
}