-- V4: Payment Transaction Tables for Spot Camp ID Backend
-- Purpose: Track payment transactions, refunds, and DOKU integration
-- Database: PostgreSQL

-- ==================== Payment Transactions ====================
-- Main table for all payment transactions
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    campsite_id BIGINT NOT NULL,

    -- Transaction identifiers
    transaction_ref VARCHAR(100) NOT NULL UNIQUE,  -- Internal reference
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    external_ref VARCHAR(200),  -- DOKU transaction ID

    -- Amount details
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    fee_amount DECIMAL(12, 2) DEFAULT 0,
    net_amount DECIMAL(12, 2),  -- Amount after fees

    -- Payment method
    payment_method VARCHAR(50) NOT NULL,  -- DOKU_VIRTUAL_ACCOUNT, DOKU_CREDIT_CARD, DOKU_QRIS, etc.
    payment_channel VARCHAR(50),  -- BCA, BNI, MANDIRI, VISA, MASTERCARD, etc.

    -- Status tracking
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    status_message VARCHAR(500),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,  -- Payment expiry
    paid_at TIMESTAMP,
    failed_at TIMESTAMP,
    refunded_at TIMESTAMP,

    -- DOKU specific fields
    doku_request_id VARCHAR(200),
    doku_response_code VARCHAR(50),
    doku_va_number VARCHAR(50),  -- Virtual Account number
    doku_qris_string TEXT,       -- QRIS string for QR code

    -- Metadata
    request_payload JSONB,   -- Raw request sent to DOKU
    response_payload JSONB,  -- Raw response from DOKU
    webhook_payload JSONB,   -- Webhook notification data
    ip_address VARCHAR(45),

    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payment_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'AWAITING_PAYMENT', 'PROCESSING', 'SUCCESS', 'FAILED', 'EXPIRED', 'CANCELLED', 'REFUNDED', 'PARTIAL_REFUND')),
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    CONSTRAINT chk_net_amount CHECK (net_amount IS NULL OR net_amount >= 0)
);

CREATE INDEX idx_payment_booking ON payment_transactions(booking_id);
CREATE INDEX idx_payment_user ON payment_transactions(user_id);
CREATE INDEX idx_payment_campsite ON payment_transactions(campsite_id);
CREATE INDEX idx_payment_status ON payment_transactions(status);
CREATE INDEX idx_payment_created ON payment_transactions(created_at);
CREATE INDEX idx_payment_method ON payment_transactions(payment_method);
CREATE INDEX idx_payment_external_ref ON payment_transactions(external_ref);
CREATE INDEX idx_payment_invoice ON payment_transactions(invoice_number);
-- CREATE INDEX idx_payment_expires ON payment_transactions(expires_at) WHERE status IN ('PENDING', 'AWAITING_PAYMENT');

-- ==================== Payment Refunds ====================
-- Track refund transactions
CREATE TABLE payment_refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_transaction_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,

    -- Refund identifiers
    refund_ref VARCHAR(100) NOT NULL UNIQUE,
    external_refund_ref VARCHAR(200),  -- DOKU refund ID

    -- Amount
    refund_amount DECIMAL(12, 2) NOT NULL,
    original_amount DECIMAL(12, 2) NOT NULL,
    refund_type VARCHAR(20) NOT NULL,  -- FULL, PARTIAL

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    status_message VARCHAR(500),

    -- Reason
    reason VARCHAR(50) NOT NULL,
    reason_detail TEXT,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,

    -- Metadata
    request_payload JSONB,
    response_payload JSONB,
    initiated_by BIGINT,  -- User ID who initiated refund (admin/merchant)

    CONSTRAINT fk_refund_payment FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id),
    CONSTRAINT fk_refund_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_refund_initiated_by FOREIGN KEY (initiated_by) REFERENCES users(id),
    CONSTRAINT chk_refund_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REJECTED')),
    CONSTRAINT chk_refund_type CHECK (refund_type IN ('FULL', 'PARTIAL')),
    CONSTRAINT chk_refund_reason CHECK (reason IN ('CUSTOMER_REQUEST', 'BOOKING_CANCELLED', 'DUPLICATE_PAYMENT', 'SERVICE_ISSUE', 'ADMIN_INITIATED', 'OTHER')),
    CONSTRAINT chk_refund_amount CHECK (refund_amount > 0 AND refund_amount <= original_amount)
);

CREATE INDEX idx_refund_payment ON payment_refunds(payment_transaction_id);
CREATE INDEX idx_refund_booking ON payment_refunds(booking_id);
CREATE INDEX idx_refund_status ON payment_refunds(status);
CREATE INDEX idx_refund_created ON payment_refunds(created_at);

-- ==================== Payment Webhooks Log ====================
-- Log all incoming webhooks for audit and debugging
CREATE TABLE payment_webhook_logs (
    id BIGSERIAL PRIMARY KEY,
    webhook_id VARCHAR(200),  -- Unique ID from payment provider
    provider VARCHAR(50) NOT NULL DEFAULT 'DOKU',
    event_type VARCHAR(50) NOT NULL,  -- payment.success, payment.failed, refund.success, etc.
    transaction_ref VARCHAR(200),
    payload JSONB NOT NULL,
    headers JSONB,
    signature VARCHAR(500),
    signature_valid BOOLEAN,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP,
    processing_result VARCHAR(500),
    ip_address VARCHAR(45),
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_log_provider ON payment_webhook_logs(provider);
CREATE INDEX idx_webhook_log_event ON payment_webhook_logs(event_type);
CREATE INDEX idx_webhook_log_transaction ON payment_webhook_logs(transaction_ref);
CREATE INDEX idx_webhook_log_received ON payment_webhook_logs(received_at);
CREATE INDEX idx_webhook_log_processed ON payment_webhook_logs(processed);

-- ==================== Merchant Payouts ====================
-- Track payouts to merchants
CREATE TABLE merchant_payouts (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    campsite_id BIGINT,  -- NULL for aggregated payouts

    -- Payout identifiers
    payout_ref VARCHAR(100) NOT NULL UNIQUE,
    external_payout_ref VARCHAR(200),

    -- Amount
    gross_amount DECIMAL(12, 2) NOT NULL,
    platform_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
    payment_gateway_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
    net_amount DECIMAL(12, 2) NOT NULL,

    -- Period
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    booking_count INTEGER NOT NULL DEFAULT 0,

    -- Bank details
    bank_name VARCHAR(100),
    bank_account_number VARCHAR(50),
    bank_account_name VARCHAR(200),

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    status_message VARCHAR(500),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,

    -- Metadata
    approved_by BIGINT,  -- Admin who approved
    notes TEXT,

    CONSTRAINT fk_payout_merchant FOREIGN KEY (merchant_id) REFERENCES users(id),
    CONSTRAINT fk_payout_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id),
    CONSTRAINT fk_payout_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT chk_payout_status CHECK (status IN ('PENDING', 'APPROVED', 'PROCESSING', 'COMPLETED', 'FAILED', 'ON_HOLD')),
    CONSTRAINT chk_payout_amounts CHECK (gross_amount >= 0 AND platform_fee >= 0 AND payment_gateway_fee >= 0 AND net_amount >= 0)
);

CREATE INDEX idx_payout_merchant ON merchant_payouts(merchant_id);
CREATE INDEX idx_payout_campsite ON merchant_payouts(campsite_id);
CREATE INDEX idx_payout_status ON merchant_payouts(status);
CREATE INDEX idx_payout_period ON merchant_payouts(period_start, period_end);
CREATE INDEX idx_payout_created ON merchant_payouts(created_at);

-- ==================== Payout Items ====================
-- Individual bookings included in a payout
CREATE TABLE payout_items (
    id BIGSERIAL PRIMARY KEY,
    payout_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    payment_transaction_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    platform_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
    net_amount DECIMAL(12, 2) NOT NULL,

    CONSTRAINT fk_payout_item_payout FOREIGN KEY (payout_id) REFERENCES merchant_payouts(id) ON DELETE CASCADE,
    CONSTRAINT fk_payout_item_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_payout_item_payment FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id)
);

CREATE INDEX idx_payout_item_payout ON payout_items(payout_id);
CREATE INDEX idx_payout_item_booking ON payout_items(booking_id);

-- ==================== Invoice Number Generator ====================
-- Generates unique invoice numbers with format: INV-YYYYMMDD-XXXXX
CREATE SEQUENCE invoice_number_seq START 1;

CREATE OR REPLACE FUNCTION generate_invoice_number()
RETURNS VARCHAR AS $$
DECLARE
    v_date VARCHAR(8);
    v_seq VARCHAR(5);
BEGIN
    v_date := TO_CHAR(NOW(), 'YYYYMMDD');
    v_seq := LPAD(NEXTVAL('invoice_number_seq')::VARCHAR, 5, '0');
    RETURN 'INV-' || v_date || '-' || v_seq;
END;
$$ LANGUAGE plpgsql;

-- Reset invoice sequence daily (call via scheduler at midnight)
CREATE OR REPLACE FUNCTION reset_daily_invoice_sequence()
RETURNS void AS $$
BEGIN
    ALTER SEQUENCE invoice_number_seq RESTART WITH 1;
END;
$$ LANGUAGE plpgsql;

-- ==================== Trigger for Payment Status Updates ====================

-- Update booking status when payment is confirmed
CREATE OR REPLACE FUNCTION update_booking_on_payment()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'SUCCESS' AND OLD.status != 'SUCCESS' THEN
        UPDATE bookings
        SET status = 'CONFIRMED',
            payment_reference = NEW.external_ref,
            confirmed_at = NEW.paid_at,
            updated_at = NOW()
        WHERE id = NEW.booking_id;

        -- Update inventory locks to CONFIRMED
        UPDATE inventory_locks
        SET lock_type = 'CONFIRMED',
            expires_at = (SELECT check_out_date + INTERVAL '1 day' FROM bookings WHERE id = NEW.booking_id)
        WHERE booking_id = NEW.booking_id AND lock_type = 'CART';
    ELSIF NEW.status = 'FAILED' AND OLD.status NOT IN ('FAILED', 'EXPIRED', 'CANCELLED') THEN
        -- Release inventory locks on payment failure
        DELETE FROM inventory_locks WHERE booking_id = NEW.booking_id AND lock_type = 'CART';
    ELSIF NEW.status = 'EXPIRED' AND OLD.status NOT IN ('FAILED', 'EXPIRED', 'CANCELLED') THEN
        -- Release inventory locks on payment expiry
        DELETE FROM inventory_locks WHERE booking_id = NEW.booking_id AND lock_type = 'CART';

        UPDATE bookings
        SET status = 'CANCELLED',
            cancelled_at = NOW(),
            updated_at = NOW()
        WHERE id = NEW.booking_id AND status = 'PAYMENT_PENDING';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_booking_on_payment
    AFTER UPDATE ON payment_transactions
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION update_booking_on_payment();
