package com.spotcamp.module.authuser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing failed login attempts for security tracking
 */
@Entity
@Table(name = "failed_login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedLoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", nullable = false, length = 50)
    private FailureReason failureReason;

    @PrePersist
    protected void onCreate() {
        if (attemptTime == null) {
            attemptTime = LocalDateTime.now();
        }
    }

    /**
     * Failure reasons for login attempts
     */
    public enum FailureReason {
        INVALID_PASSWORD,
        USER_NOT_FOUND,
        ACCOUNT_LOCKED,
        ACCOUNT_SUSPENDED,
        EMAIL_NOT_VERIFIED
    }
}
