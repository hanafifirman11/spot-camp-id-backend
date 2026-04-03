package com.spotcamp.module.authuser.entity;

import com.spotcamp.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * User entity representing both campers and merchants
 * Implements the user requirements from the OpenAPI specification
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.CAMPER;

    @Column(name = "business_name", length = 200)
    private String businessName;

    @Column(name = "business_code", length = 20)
    private String businessCode;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "dark_mode", nullable = false)
    private boolean darkMode = false;

    /**
     * Gets the full name of the user
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Checks if the user is a merchant
     */
    public boolean isMerchant() {
        return role == UserRole.MERCHANT || role == UserRole.MERCHANT_ADMIN || role == UserRole.MERCHANT_MEMBER;
    }

    /**
     * Checks if the user is active
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Updates the last login timestamp
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Validates business information for merchants
     */
    public boolean isValidMerchant() {
        return role == UserRole.MERCHANT && businessName != null && !businessName.trim().isEmpty();
    }
}
