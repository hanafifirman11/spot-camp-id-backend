package com.spotcamp.authuser.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing user sessions for security auditing
 */
@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false, unique = true, length = 255)
    private String sessionId;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(name = "logged_out_at")
    private LocalDateTime loggedOutAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastActivityAt == null) {
            lastActivityAt = LocalDateTime.now();
        }
    }

    /**
     * Updates the last activity timestamp
     */
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Ends this session
     */
    public void endSession() {
        this.active = false;
        this.loggedOutAt = LocalDateTime.now();
    }

    /**
     * Checks if session is inactive for given minutes
     */
    public boolean isInactiveFor(int minutes) {
        return LocalDateTime.now().isAfter(lastActivityAt.plusMinutes(minutes));
    }
}
