package com.spotcamp.common.security;

import com.spotcamp.authuser.domain.User;
import com.spotcamp.authuser.domain.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Facade for getting current user information from security context
 */
@Component
public class AuthenticationFacade {

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUser();
        }
        throw new IllegalStateException("No authenticated user found");
    }

    /**
     * Get current user ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Get current user email
     */
    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Get current user role
     */
    public UserRole getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(UserRole role) {
        try {
            return getCurrentUserRole() == role;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Check if current user is merchant
     */
    public boolean isMerchant() {
        return hasRole(UserRole.MERCHANT) || hasRole(UserRole.MERCHANT_ADMIN) || hasRole(UserRole.MERCHANT_MEMBER);
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole(UserRole.SUPERADMIN) || hasRole(UserRole.ADMIN);
    }

    /**
     * Check if current user is camper
     */
    public boolean isCamper() {
        return hasRole(UserRole.CAMPER);
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }
}
