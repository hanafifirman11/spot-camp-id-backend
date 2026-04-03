package com.spotcamp.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all custom exceptions in the application
 */
@Getter
public abstract class BaseException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Object[] args;

    protected BaseException(String errorCode, HttpStatus httpStatus, String message) {
        this(errorCode, httpStatus, message, new Object[0]);
    }

    protected BaseException(String errorCode, HttpStatus httpStatus, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.args = args;
    }

    protected BaseException(String errorCode, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.args = new Object[0];
    }
}