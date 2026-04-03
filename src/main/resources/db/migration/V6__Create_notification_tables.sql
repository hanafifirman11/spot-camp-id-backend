-- V6: Notification Tables for Spot Camp ID Backend
-- Purpose: User notifications, email logs, and push notification management
-- Database: PostgreSQL

-- ==================== User Notifications ====================
-- In-app notifications for users
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,  -- BOOKING_CONFIRMED, PAYMENT_SUCCESS, REVIEW_REQUEST, etc.
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,  -- Additional data (booking_id, campsite_id, etc.)

    -- Reference
    reference_type VARCHAR(50),  -- BOOKING, PAYMENT, REVIEW, CAMPSITE, etc.
    reference_id BIGINT,

    -- Status
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,

    -- Priority
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',  -- LOW, NORMAL, HIGH, URGENT

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,  -- Optional expiry

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_notification_type CHECK (type IN (
        'BOOKING_CREATED', 'BOOKING_CONFIRMED', 'BOOKING_CANCELLED', 'BOOKING_REMINDER',
        'PAYMENT_SUCCESS', 'PAYMENT_FAILED', 'PAYMENT_REFUNDED',
        'REVIEW_REQUEST', 'REVIEW_RESPONSE',
        'PROMO_CODE', 'SYSTEM_ANNOUNCEMENT',
        'MERCHANT_NEW_BOOKING', 'MERCHANT_PAYOUT', 'MERCHANT_LOW_STOCK'
    )),
    CONSTRAINT chk_notification_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'))
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
-- CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created ON notifications(created_at);
CREATE INDEX idx_notifications_reference ON notifications(reference_type, reference_id);

-- ==================== Notification Preferences ====================
-- User preferences for notification channels
CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    -- Email preferences
    email_booking_confirmation BOOLEAN NOT NULL DEFAULT TRUE,
    email_payment_receipt BOOLEAN NOT NULL DEFAULT TRUE,
    email_booking_reminder BOOLEAN NOT NULL DEFAULT TRUE,
    email_review_request BOOLEAN NOT NULL DEFAULT TRUE,
    email_promotions BOOLEAN NOT NULL DEFAULT TRUE,
    email_newsletter BOOLEAN NOT NULL DEFAULT FALSE,

    -- Push notification preferences
    push_booking_updates BOOLEAN NOT NULL DEFAULT TRUE,
    push_payment_updates BOOLEAN NOT NULL DEFAULT TRUE,
    push_promotions BOOLEAN NOT NULL DEFAULT FALSE,

    -- SMS preferences (future)
    sms_booking_confirmation BOOLEAN NOT NULL DEFAULT FALSE,
    sms_payment_confirmation BOOLEAN NOT NULL DEFAULT FALSE,

    -- Merchant specific (only for merchants)
    merchant_new_booking_email BOOLEAN NOT NULL DEFAULT TRUE,
    merchant_new_booking_push BOOLEAN NOT NULL DEFAULT TRUE,
    merchant_daily_summary BOOLEAN NOT NULL DEFAULT TRUE,
    merchant_low_stock_alert BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_prefs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_prefs_user ON notification_preferences(user_id);

-- ==================== Email Logs ====================
-- Track all sent emails
CREATE TABLE email_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    template_id VARCHAR(100) NOT NULL,
    subject VARCHAR(500) NOT NULL,

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    status_message TEXT,

    -- Content
    template_data JSONB,  -- Variables passed to template
    html_content TEXT,    -- Rendered HTML (optional, for debugging)

    -- Email provider info
    provider VARCHAR(50) DEFAULT 'SENDGRID',
    provider_message_id VARCHAR(200),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    bounced_at TIMESTAMP,
    failed_at TIMESTAMP,

    -- Tracking
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_retry_at TIMESTAMP,

    CONSTRAINT fk_email_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_email_status CHECK (status IN ('PENDING', 'QUEUED', 'SENT', 'DELIVERED', 'OPENED', 'CLICKED', 'BOUNCED', 'FAILED', 'SPAM'))
);

CREATE INDEX idx_email_logs_user ON email_logs(user_id);
CREATE INDEX idx_email_logs_recipient ON email_logs(recipient_email);
CREATE INDEX idx_email_logs_status ON email_logs(status);
CREATE INDEX idx_email_logs_template ON email_logs(template_id);
CREATE INDEX idx_email_logs_created ON email_logs(created_at);
CREATE INDEX idx_email_logs_provider_id ON email_logs(provider_message_id);

-- ==================== Push Notification Tokens ====================
-- Store device tokens for push notifications
CREATE TABLE push_notification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    device_type VARCHAR(20) NOT NULL,  -- IOS, ANDROID, WEB
    device_name VARCHAR(200),
    device_id VARCHAR(200),  -- Unique device identifier

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_push_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_device_type CHECK (device_type IN ('IOS', 'ANDROID', 'WEB')),
    CONSTRAINT uq_push_token UNIQUE (user_id, token)
);

CREATE INDEX idx_push_tokens_user ON push_notification_tokens(user_id);
CREATE INDEX idx_push_tokens_active ON push_notification_tokens(user_id, is_active);

-- ==================== Push Notification Logs ====================
-- Track sent push notifications
CREATE TABLE push_notification_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_id BIGINT,
    notification_id BIGINT,  -- Reference to notifications table

    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    data JSONB,

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    status_message TEXT,

    -- Provider info
    provider VARCHAR(50) DEFAULT 'FCM',  -- Firebase Cloud Messaging
    provider_message_id VARCHAR(200),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,

    CONSTRAINT fk_push_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_push_logs_token FOREIGN KEY (token_id) REFERENCES push_notification_tokens(id) ON DELETE SET NULL,
    CONSTRAINT fk_push_logs_notification FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE SET NULL,
    CONSTRAINT chk_push_status CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'INVALID_TOKEN'))
);

CREATE INDEX idx_push_logs_user ON push_notification_logs(user_id);
CREATE INDEX idx_push_logs_status ON push_notification_logs(status);
CREATE INDEX idx_push_logs_created ON push_notification_logs(created_at);

-- ==================== System Announcements ====================
-- Platform-wide announcements
CREATE TABLE system_announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,  -- INFO, WARNING, MAINTENANCE, PROMOTION
    target_audience VARCHAR(30) NOT NULL DEFAULT 'ALL',  -- ALL, CAMPERS, MERCHANTS

    -- Display settings
    display_from TIMESTAMP NOT NULL,
    display_until TIMESTAMP NOT NULL,
    is_dismissible BOOLEAN NOT NULL DEFAULT TRUE,
    priority INTEGER NOT NULL DEFAULT 0,

    -- Optional link
    action_url VARCHAR(500),
    action_text VARCHAR(100),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    CONSTRAINT chk_announcement_type CHECK (type IN ('INFO', 'WARNING', 'MAINTENANCE', 'PROMOTION', 'UPDATE')),
    CONSTRAINT chk_announcement_audience CHECK (target_audience IN ('ALL', 'CAMPERS', 'MERCHANTS', 'ADMINS')),
    CONSTRAINT chk_announcement_dates CHECK (display_until > display_from)
);

CREATE INDEX idx_announcements_active ON system_announcements(is_active, display_from, display_until);
CREATE INDEX idx_announcements_audience ON system_announcements(target_audience);

-- ==================== Dismissed Announcements ====================
-- Track which users dismissed which announcements
CREATE TABLE dismissed_announcements (
    user_id BIGINT NOT NULL,
    announcement_id BIGINT NOT NULL,
    dismissed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, announcement_id),
    CONSTRAINT fk_dismissed_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_dismissed_announcement FOREIGN KEY (announcement_id) REFERENCES system_announcements(id) ON DELETE CASCADE
);

-- ==================== Booking Reminders ====================
-- Schedule reminders for upcoming bookings
CREATE TABLE booking_reminders (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reminder_type VARCHAR(50) NOT NULL,  -- CHECK_IN_24H, CHECK_IN_1H, REVIEW_REQUEST
    scheduled_at TIMESTAMP NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reminder_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_reminder_type CHECK (reminder_type IN ('CHECK_IN_24H', 'CHECK_IN_1H', 'CHECK_OUT_REMINDER', 'REVIEW_REQUEST')),
    CONSTRAINT uq_booking_reminder UNIQUE (booking_id, reminder_type)
);

CREATE INDEX idx_reminders_scheduled ON booking_reminders(scheduled_at, sent);
CREATE INDEX idx_reminders_booking ON booking_reminders(booking_id);
CREATE INDEX idx_reminders_user ON booking_reminders(user_id);

-- ==================== Functions ====================

-- Function to create default notification preferences for new users
CREATE OR REPLACE FUNCTION create_default_notification_prefs()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO notification_preferences (user_id)
    VALUES (NEW.id)
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_notification_prefs
    AFTER INSERT ON users
    FOR EACH ROW EXECUTE FUNCTION create_default_notification_prefs();

-- Function to create booking reminders
CREATE OR REPLACE FUNCTION create_booking_reminders()
RETURNS TRIGGER AS $$
BEGIN
    -- Only create reminders for confirmed bookings
    IF NEW.status = 'CONFIRMED' AND (OLD IS NULL OR OLD.status != 'CONFIRMED') THEN
        -- 24 hours before check-in
        INSERT INTO booking_reminders (booking_id, user_id, reminder_type, scheduled_at)
        VALUES (NEW.id, NEW.user_id, 'CHECK_IN_24H', NEW.check_in_date::TIMESTAMP - INTERVAL '24 hours')
        ON CONFLICT DO NOTHING;

        -- 1 hour before check-in
        INSERT INTO booking_reminders (booking_id, user_id, reminder_type, scheduled_at)
        VALUES (NEW.id, NEW.user_id, 'CHECK_IN_1H', NEW.check_in_date::TIMESTAMP - INTERVAL '1 hour')
        ON CONFLICT DO NOTHING;

        -- Review request 1 day after checkout
        INSERT INTO booking_reminders (booking_id, user_id, reminder_type, scheduled_at)
        VALUES (NEW.id, NEW.user_id, 'REVIEW_REQUEST', NEW.check_out_date::TIMESTAMP + INTERVAL '1 day')
        ON CONFLICT DO NOTHING;
    END IF;

    -- Delete reminders if booking is cancelled
    IF NEW.status = 'CANCELLED' THEN
        DELETE FROM booking_reminders WHERE booking_id = NEW.id AND sent = FALSE;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_booking_reminders
    AFTER INSERT OR UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION create_booking_reminders();

-- Function to mark notifications as read
CREATE OR REPLACE FUNCTION mark_notifications_read(p_user_id BIGINT, p_notification_ids BIGINT[] DEFAULT NULL)
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    IF p_notification_ids IS NULL THEN
        -- Mark all as read
        UPDATE notifications
        SET is_read = TRUE, read_at = NOW()
        WHERE user_id = p_user_id AND is_read = FALSE;
    ELSE
        -- Mark specific notifications as read
        UPDATE notifications
        SET is_read = TRUE, read_at = NOW()
        WHERE user_id = p_user_id AND id = ANY(p_notification_ids) AND is_read = FALSE;
    END IF;

    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update notification_preferences updated_at
CREATE TRIGGER update_notification_prefs_updated_at
    BEFORE UPDATE ON notification_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
