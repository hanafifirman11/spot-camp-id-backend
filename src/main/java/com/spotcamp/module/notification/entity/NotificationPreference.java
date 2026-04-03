package com.spotcamp.module.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing user notification preferences
 */
@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // Email preferences
    @Column(name = "email_booking_confirmation", nullable = false)
    private boolean emailBookingConfirmation = true;

    @Column(name = "email_payment_receipt", nullable = false)
    private boolean emailPaymentReceipt = true;

    @Column(name = "email_booking_reminder", nullable = false)
    private boolean emailBookingReminder = true;

    @Column(name = "email_review_request", nullable = false)
    private boolean emailReviewRequest = true;

    @Column(name = "email_promotions", nullable = false)
    private boolean emailPromotions = true;

    @Column(name = "email_newsletter", nullable = false)
    private boolean emailNewsletter = false;

    // Push notification preferences
    @Column(name = "push_booking_updates", nullable = false)
    private boolean pushBookingUpdates = true;

    @Column(name = "push_payment_updates", nullable = false)
    private boolean pushPaymentUpdates = true;

    @Column(name = "push_promotions", nullable = false)
    private boolean pushPromotions = false;

    // SMS preferences
    @Column(name = "sms_booking_confirmation", nullable = false)
    private boolean smsBookingConfirmation = false;

    @Column(name = "sms_payment_confirmation", nullable = false)
    private boolean smsPaymentConfirmation = false;

    // Merchant specific preferences
    @Column(name = "merchant_new_booking_email", nullable = false)
    private boolean merchantNewBookingEmail = true;

    @Column(name = "merchant_new_booking_push", nullable = false)
    private boolean merchantNewBookingPush = true;

    @Column(name = "merchant_daily_summary", nullable = false)
    private boolean merchantDailySummary = true;

    @Column(name = "merchant_low_stock_alert", nullable = false)
    private boolean merchantLowStockAlert = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enables all email notifications
     */
    public void enableAllEmails() {
        this.emailBookingConfirmation = true;
        this.emailPaymentReceipt = true;
        this.emailBookingReminder = true;
        this.emailReviewRequest = true;
        this.emailPromotions = true;
        this.emailNewsletter = true;
    }

    /**
     * Disables all marketing emails
     */
    public void disableMarketingEmails() {
        this.emailPromotions = false;
        this.emailNewsletter = false;
        this.pushPromotions = false;
    }
}
