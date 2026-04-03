package com.spotcamp.notification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing platform-wide announcements
 */
@Entity
@Table(name = "system_announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private AnnouncementType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", nullable = false, length = 30)
    private TargetAudience targetAudience = TargetAudience.ALL;

    @Column(name = "display_from", nullable = false)
    private LocalDateTime displayFrom;

    @Column(name = "display_until", nullable = false)
    private LocalDateTime displayUntil;

    @Column(name = "is_dismissible", nullable = false)
    private boolean dismissible = true;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "action_text", length = 100)
    private String actionText;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if announcement is currently visible
     */
    public boolean isCurrentlyVisible() {
        LocalDateTime now = LocalDateTime.now();
        return active && now.isAfter(displayFrom) && now.isBefore(displayUntil);
    }

    /**
     * Checks if announcement targets specific audience
     */
    public boolean isForAudience(TargetAudience audience) {
        return targetAudience == TargetAudience.ALL || targetAudience == audience;
    }

    /**
     * Announcement types
     */
    public enum AnnouncementType {
        INFO,
        WARNING,
        MAINTENANCE,
        PROMOTION,
        UPDATE
    }

    /**
     * Target audience
     */
    public enum TargetAudience {
        ALL,
        CAMPERS,
        MERCHANTS,
        ADMINS
    }
}
