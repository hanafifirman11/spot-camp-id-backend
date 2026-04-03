package com.spotcamp.common.security;

import com.spotcamp.authuser.domain.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for role-based access control
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    UserRole[] value();
    boolean requireAll() default false; // If true, user must have ALL roles; if false, user must have ANY role
}