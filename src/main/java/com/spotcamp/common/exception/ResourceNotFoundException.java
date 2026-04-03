package com.spotcamp.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BaseException {
    
    public ResourceNotFoundException(String resourceType, Object id) {
        super("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, 
              String.format("%s with id %s not found", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND,
              String.format("%s with %s %s not found", resourceType, fieldName, fieldValue));
    }
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, message);
    }
}
