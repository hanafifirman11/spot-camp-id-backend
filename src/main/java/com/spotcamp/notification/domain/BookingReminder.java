package com.spotcamp.notification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing scheduled booking reminders
 */
@Entity
@Table(name = "booking_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 50)
    private ReminderType reminderType;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "sent", nullable = false)
    private boolean sent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if reminder is due to be sent
     */
    public boolean isDue() {
        return !sent && LocalDateTime.now().isAfter(scheduledAt);
    }

    /**
     * Marks reminder as sent
     */
    public void markAsSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Reminder types
     */
    public enum ReminderType {
        CHECK_IN_24H,
        CHECK_IN_1H,
        CHECK_OUT_REMINDER,
        REVIEW_REQUEST
    }
}
