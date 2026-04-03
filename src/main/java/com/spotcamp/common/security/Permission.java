package com.spotcamp.common.security;

/**
 * Permission constants for the application
 */
public final class Permission {
    
    // Admin permissions
    public static final String ADMIN_ALL = "PERMISSION_ADMIN_ALL";
    
    // Merchant permissions
    public static final String MERCHANT_ALL = "PERMISSION_MERCHANT_ALL";
    public static final String INVENTORY_MANAGE = "PERMISSION_INVENTORY_MANAGE";
    public static final String BOOKING_MANAGE = "PERMISSION_BOOKING_MANAGE";
    public static final String REPORTS_VIEW = "PERMISSION_REPORTS_VIEW";
    public static final String CAMPSITE_MANAGE = "PERMISSION_CAMPSITE_MANAGE";
    
    // Camper permissions
    public static final String CAMPER_ALL = "PERMISSION_CAMPER_ALL";
    public static final String BOOKING_CREATE = "PERMISSION_BOOKING_CREATE";
    public static final String PROFILE_MANAGE = "PERMISSION_PROFILE_MANAGE";
    
    // Public permissions
    public static final String PUBLIC_READ = "PERMISSION_PUBLIC_READ";
    
    private Permission() {
        // Utility class
    }
}