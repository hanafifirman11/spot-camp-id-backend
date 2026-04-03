-- V2: CQRS Reporting Tables for Spot Camp ID Backend
-- Purpose: Read models for optimized query performance (CQRS pattern)
-- Database: PostgreSQL

-- ==================== Booking Reports (Read Model) ====================
-- Denormalized view of bookings for fast query and reporting
CREATE TABLE booking_reports (
    booking_id BIGINT PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    campsite_name VARCHAR(200),
    user_id BIGINT NOT NULL,
    user_email VARCHAR(255),
    spot_id VARCHAR(100),
    spot_name VARCHAR(200),
    booking_date DATE NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    nights INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(50),
    booking_month VARCHAR(7) NOT NULL,  -- YYYY-MM format
    booking_year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    total_items INTEGER NOT NULL DEFAULT 0,
    has_products BOOLEAN NOT NULL DEFAULT FALSE,
    revenue_month VARCHAR(7) NOT NULL,  -- Month when revenue recognized
    is_weekend_booking BOOLEAN NOT NULL DEFAULT FALSE,
    advance_days INTEGER NOT NULL DEFAULT 0,
    season VARCHAR(20),  -- HIGH, MEDIUM, LOW

    CONSTRAINT fk_booking_reports_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT chk_booking_report_status CHECK (status IN ('IN_CART', 'PAYMENT_PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_booking_report_season CHECK (season IS NULL OR season IN ('HIGH', 'MEDIUM', 'LOW'))
);

CREATE INDEX idx_booking_report_campsite_date ON booking_reports(campsite_id, booking_date);
CREATE INDEX idx_booking_report_status ON booking_reports(status);
CREATE INDEX idx_booking_report_created ON booking_reports(created_at);
CREATE INDEX idx_booking_report_month ON booking_reports(booking_month);
CREATE INDEX idx_booking_report_year ON booking_reports(booking_year);
CREATE INDEX idx_booking_report_user ON booking_reports(user_id);
CREATE INDEX idx_booking_report_revenue_month ON booking_reports(revenue_month);
CREATE INDEX idx_booking_report_season ON booking_reports(season);

-- ==================== Revenue Reports (Aggregated Read Model) ====================
-- Pre-calculated revenue metrics for dashboard performance
CREATE TABLE revenue_reports (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT,  -- NULL for system-wide reports
    campsite_name VARCHAR(200),
    period_type VARCHAR(20) NOT NULL,  -- DAILY, WEEKLY, MONTHLY, YEARLY
    period_key VARCHAR(20) NOT NULL,   -- 2024-01-15, 2024-W03, 2024-01, 2024
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,

    -- Revenue metrics
    total_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0,
    spot_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0,
    product_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0,

    -- Booking metrics
    total_bookings INTEGER NOT NULL DEFAULT 0,
    confirmed_bookings INTEGER NOT NULL DEFAULT 0,
    cancelled_bookings INTEGER NOT NULL DEFAULT 0,
    total_nights INTEGER NOT NULL DEFAULT 0,
    average_booking_value DECIMAL(8, 2),
    occupancy_rate DECIMAL(5, 2),  -- Percentage

    -- Customer metrics
    unique_customers INTEGER NOT NULL DEFAULT 0,
    new_customers INTEGER NOT NULL DEFAULT 0,
    returning_customers INTEGER NOT NULL DEFAULT 0,

    -- Seasonal metrics
    weekend_bookings INTEGER NOT NULL DEFAULT 0,
    weekday_bookings INTEGER NOT NULL DEFAULT 0,
    weekend_revenue DECIMAL(12, 2) DEFAULT 0,
    weekday_revenue DECIMAL(12, 2) DEFAULT 0,

    -- Performance metrics
    advance_booking_days DECIMAL(6, 2),  -- Average advance booking days
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_revenue_reports_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE,
    CONSTRAINT chk_period_type CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT chk_occupancy_rate CHECK (occupancy_rate IS NULL OR (occupancy_rate >= 0 AND occupancy_rate <= 100)),
    CONSTRAINT uq_revenue_report UNIQUE (campsite_id, period_type, period_key)
);

CREATE INDEX idx_revenue_campsite_period ON revenue_reports(campsite_id, period_type, period_key);
CREATE INDEX idx_revenue_period ON revenue_reports(period_type, period_key);
CREATE INDEX idx_revenue_period_start ON revenue_reports(period_start);
CREATE INDEX idx_revenue_last_updated ON revenue_reports(last_updated);

-- ==================== Spot Availability Cache ====================
-- Real-time availability tracking for spots
CREATE TABLE spot_availability (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    spot_id VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    booking_id BIGINT,  -- NULL if available
    price_override DECIMAL(12, 2),  -- For dynamic pricing
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_spot_availability_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE,
    CONSTRAINT fk_spot_availability_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    CONSTRAINT uq_spot_availability UNIQUE (campsite_id, spot_id, date)
);

CREATE INDEX idx_spot_availability_campsite ON spot_availability(campsite_id);
CREATE INDEX idx_spot_availability_date ON spot_availability(date);
CREATE INDEX idx_spot_availability_available ON spot_availability(campsite_id, date, is_available);

-- ==================== Functions for CQRS Sync ====================

-- Function to sync booking to booking_reports
CREATE OR REPLACE FUNCTION sync_booking_report()
RETURNS TRIGGER AS $$
DECLARE
    v_campsite_name VARCHAR(200);
    v_user_email VARCHAR(255);
    v_total_items INTEGER;
    v_has_products BOOLEAN;
    v_day_of_week INTEGER;
BEGIN
    -- Get campsite name
    SELECT name INTO v_campsite_name FROM campsites WHERE id = NEW.campsite_id;

    -- Get user email
    SELECT email INTO v_user_email FROM users WHERE id = NEW.user_id;

    -- Count booking items
    SELECT COUNT(*), COALESCE(SUM(CASE WHEN product_type = 'SALE' THEN 1 ELSE 0 END) > 0, FALSE)
    INTO v_total_items, v_has_products
    FROM booking_items WHERE booking_id = NEW.id;

    -- Calculate day of week (1=Monday, 7=Sunday)
    v_day_of_week := EXTRACT(ISODOW FROM NEW.check_in_date);

    -- Insert or update booking report
    INSERT INTO booking_reports (
        booking_id, campsite_id, campsite_name, user_id, user_email,
        spot_id, spot_name, booking_date, check_in_date, check_out_date,
        nights, status, total_amount, payment_method, booking_month,
        booking_year, created_at, confirmed_at, cancelled_at, total_items,
        has_products, revenue_month, is_weekend_booking, advance_days, season
    ) VALUES (
        NEW.id, NEW.campsite_id, v_campsite_name, NEW.user_id, v_user_email,
        NEW.spot_id, NEW.spot_name, DATE(NEW.created_at), NEW.check_in_date, NEW.check_out_date,
        (NEW.check_out_date - NEW.check_in_date), NEW.status, NEW.total_amount, NEW.payment_method,
        TO_CHAR(NEW.created_at, 'YYYY-MM'), EXTRACT(YEAR FROM NEW.created_at),
        NEW.created_at, NEW.confirmed_at, NEW.cancelled_at, COALESCE(v_total_items, 0),
        COALESCE(v_has_products, FALSE),
        COALESCE(TO_CHAR(NEW.confirmed_at, 'YYYY-MM'), TO_CHAR(NEW.created_at, 'YYYY-MM')),
        (v_day_of_week IN (6, 7)),
        (NEW.check_in_date - DATE(NEW.created_at)),
        CASE
            WHEN EXTRACT(MONTH FROM NEW.check_in_date) IN (6, 7, 8) THEN 'HIGH'
            WHEN EXTRACT(MONTH FROM NEW.check_in_date) IN (3, 4, 5, 9, 10, 11) THEN 'MEDIUM'
            ELSE 'LOW'
        END
    )
    ON CONFLICT (booking_id) DO UPDATE SET
        campsite_name = EXCLUDED.campsite_name,
        user_email = EXCLUDED.user_email,
        spot_id = EXCLUDED.spot_id,
        spot_name = EXCLUDED.spot_name,
        check_in_date = EXCLUDED.check_in_date,
        check_out_date = EXCLUDED.check_out_date,
        nights = EXCLUDED.nights,
        status = EXCLUDED.status,
        total_amount = EXCLUDED.total_amount,
        payment_method = EXCLUDED.payment_method,
        confirmed_at = EXCLUDED.confirmed_at,
        cancelled_at = EXCLUDED.cancelled_at,
        total_items = EXCLUDED.total_items,
        has_products = EXCLUDED.has_products,
        revenue_month = EXCLUDED.revenue_month;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to sync bookings to booking_reports
CREATE TRIGGER trg_sync_booking_report
    AFTER INSERT OR UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION sync_booking_report();

-- Trigger to update revenue_reports last_updated
CREATE TRIGGER update_revenue_reports_updated_at
    BEFORE UPDATE ON revenue_reports
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
