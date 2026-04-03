package com.spotcamp.security;

import com.spotcamp.module.authuser.entity.User;
import com.spotcamp.module.authuser.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User principal implementation for Spring Security
 */
@AllArgsConstructor
@Getter
public class UserPrincipal implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role-based authority
        UserRole role = user.getRole();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        
        // Add specific permissions based on role
        switch (role) {
            case SUPERADMIN:
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_ADMIN_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MERCHANT_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_CAMPER_ALL"));
                break;
            case MERCHANT:
            case MERCHANT_ADMIN:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MERCHANT_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_CAMPER_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_INVENTORY_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_BOOKING_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_REPORTS_VIEW"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_USER_MANAGE"));
                break;
            case MERCHANT_MEMBER:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_CAMPER_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_INVENTORY_VIEW"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_BOOKING_MANAGE")); // Members can manage bookings
                authorities.add(new SimpleGrantedAuthority("PERMISSION_REPORTS_VIEW"));
                break;
            case CAMPER:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_CAMPER_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_BOOKING_CREATE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_PROFILE_MANAGE"));
                break;
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Can be enhanced with expiration logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive(); // Account is not locked if user is active
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Can be enhanced with password expiration logic
    }

    @Override
    public boolean isEnabled() {
        return user.isActive() && user.isEmailVerified();
    }

    /**
     * Get user ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * Convenience accessor for compatibility with controllers/services.
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Convenience accessor for compatibility with controllers/services.
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Get user role
     */
    public UserRole getRole() {
        return user.getRole();
    }

    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(String permission) {
        return getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
    }

    /**
     * Check if user is merchant
     */
    public boolean isMerchant() {
        return user.getRole() == UserRole.MERCHANT || user.getRole() == UserRole.MERCHANT_ADMIN || user.getRole() == UserRole.MERCHANT_MEMBER;
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return user.getRole() == UserRole.SUPERADMIN || user.getRole() == UserRole.ADMIN;
    }

    /**
     * Check if user is camper
     */
    public boolean isCamper() {
        return user.getRole() == UserRole.CAMPER;
    }
}
