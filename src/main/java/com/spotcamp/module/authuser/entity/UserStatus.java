package com.spotcamp.module.authuser.entity;

/**
 * User account status
 */
public enum UserStatus {
    /**
     * Active user with full access
     */
    ACTIVE,
    
    /**
     * Temporarily inactive user
     */
    INACTIVE,
    
    /**
     * Suspended user due to policy violation
     */
    SUSPENDED
}