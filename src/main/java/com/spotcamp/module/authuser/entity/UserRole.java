package com.spotcamp.module.authuser.entity;

/**
 * User roles in the system
 */
public enum UserRole {
    /**
     * Regular users who book campsites
     */
    CAMPER,

    /**
     * Legacy merchant role (deprecated, use MERCHANT_ADMIN or MERCHANT_MEMBER)
     * @deprecated Use MERCHANT_ADMIN or MERCHANT_MEMBER instead
     */
    @Deprecated
    MERCHANT,

    /**
     * Merchant Administrator with full access to business settings, master data, and user management
     */
    MERCHANT_ADMIN,

    /**
     * Merchant Member with limited access: can manage bookings and view reports, read-only access to master data
     */
    MERCHANT_MEMBER,

    /**
     * System superadmins with full access
     */
    SUPERADMIN,

    /**
     * Legacy system administrator role (deprecated, use SUPERADMIN)
     * @deprecated Use SUPERADMIN instead
     */
    @Deprecated
    ADMIN
}
