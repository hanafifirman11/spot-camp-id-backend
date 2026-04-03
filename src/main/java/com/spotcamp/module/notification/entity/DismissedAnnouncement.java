package com.spotcamp.module.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity tracking which users dismissed which announcements
 */
@Entity
@Table(name = "dismissed_announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(DismissedAnnouncement.DismissedAnnouncementId.class)
public class DismissedAnnouncement {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "announcement_id", nullable = false)
    private Long announcementId;

    @Column(name = "dismissed_at", nullable = false)
    private LocalDateTime dismissedAt;

    @PrePersist
    protected void onCreate() {
        if (dismissedAt == null) {
            dismissedAt = LocalDateTime.now();
        }
    }

    /**
     * Composite primary key class
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DismissedAnnouncementId implements Serializable {
        private Long userId;
        private Long announcementId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DismissedAnnouncementId that)) return false;
            return userId.equals(that.userId) && announcementId.equals(that.announcementId);
        }

        @Override
        public int hashCode() {
            return 31 * userId.hashCode() + announcementId.hashCode();
        }
    }
}
