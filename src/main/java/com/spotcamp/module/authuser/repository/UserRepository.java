package com.spotcamp.module.authuser.repository;

import com.spotcamp.module.authuser.entity.User;
import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Repository for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Check if email exists (case insensitive)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    /**
     * Find active user by email
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.status = :status")
    Optional<User> findByEmailAndStatus(@Param("email") String email, @Param("status") UserStatus status);

    /**
     * Update last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Count users by role
     */
    long countByRole(UserRole role);

    /**
     * Count users by role and status
     */
    long countByRoleAndStatus(UserRole role, UserStatus status);

    /**
     * Check if business code exists
     */
    boolean existsByBusinessCode(String businessCode);

    /**
     * Find users by business code
     */
    java.util.List<User> findByBusinessCode(String businessCode);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find users by role
     */
    java.util.List<User> findByRole(UserRole role);

    /**
     * Find users by role with pagination
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * Find users by role and status with pagination
     */
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    /**
     * Find users by roles with pagination
     */
    Page<User> findByRoleIn(java.util.List<UserRole> roles, Pageable pageable);

    /**
     * Find users by roles and status with pagination
     */
    Page<User> findByRoleInAndStatus(java.util.List<UserRole> roles, UserStatus status, Pageable pageable);

    /**
     * Count users by status
     */
    long countByStatus(UserStatus status);

    /**
     * Find merchants (for admin purposes)
     */
    @Query("SELECT u FROM User u WHERE (u.role = 'MERCHANT' OR u.role = 'MERCHANT_ADMIN') AND u.status = :status ORDER BY u.createdAt DESC")
    java.util.List<User> findMerchantsByStatus(@Param("status") UserStatus status);

    /**
     * Search merchants by query and optional status
     */
    @Query("""
        SELECT u FROM User u
        WHERE (u.role = 'MERCHANT' OR u.role = 'MERCHANT_ADMIN')
        AND (:status IS NULL OR u.status = :status)
        AND (
            LOWER(u.businessName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.businessCode) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY u.createdAt DESC
        """)
    Page<User> searchMerchants(@Param("query") String query, @Param("status") UserStatus status, Pageable pageable);

    /**
     * Admin search across all users with optional filters.
     */
    @Query("""
        SELECT u FROM User u
        WHERE (:role IS NULL OR u.role = :role)
        AND (:status IS NULL OR u.status = :status)
        AND (
            :query IS NULL
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.businessName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.businessCode) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY u.createdAt DESC
        """)
    Page<User> searchUsers(@Param("query") String query,
                           @Param("status") UserStatus status,
                           @Param("role") UserRole role,
                           Pageable pageable);
}
