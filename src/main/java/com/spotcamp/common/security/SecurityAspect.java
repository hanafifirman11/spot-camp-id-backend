package com.spotcamp.common.security;

import com.spotcamp.authuser.domain.UserRole;
import com.spotcamp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect for handling custom security annotations
 */
@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {

    private final AuthenticationFacade authenticationFacade;

    @Around("@annotation(requireRole)")
    public Object checkRoleAccess(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        if (!authenticationFacade.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        UserRole userRole = authenticationFacade.getCurrentUserRole();
        UserRole[] requiredRoles = requireRole.value();
        boolean requireAll = requireRole.requireAll();

        boolean hasAccess;
        if (requireAll) {
            // User must have ALL required roles (not typical for most use cases)
            hasAccess = Arrays.asList(requiredRoles).contains(userRole);
        } else {
            // User must have ANY of the required roles
            hasAccess = Arrays.asList(requiredRoles).contains(userRole);
        }

        if (!hasAccess) {
            throw new AccessDeniedException("Insufficient role permissions");
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requirePermission)")
    public Object checkPermissionAccess(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        if (!authenticationFacade.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        UserPrincipal userPrincipal = (UserPrincipal) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        String[] requiredPermissions = requirePermission.value();
        boolean requireAll = requirePermission.requireAll();

        boolean hasAccess;
        if (requireAll) {
            // User must have ALL required permissions
            hasAccess = Arrays.stream(requiredPermissions)
                    .allMatch(userPrincipal::hasPermission);
        } else {
            // User must have ANY of the required permissions
            hasAccess = Arrays.stream(requiredPermissions)
                    .anyMatch(userPrincipal::hasPermission);
        }

        if (!hasAccess) {
            throw new AccessDeniedException("Insufficient permissions");
        }

        return joinPoint.proceed();
    }

    @Around("@within(requireRole)")
    public Object checkClassRoleAccess(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        return checkRoleAccess(joinPoint, requireRole);
    }

    @Around("@within(requirePermission)")
    public Object checkClassPermissionAccess(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        return checkPermissionAccess(joinPoint, requirePermission);
    }
}