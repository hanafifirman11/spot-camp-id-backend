-- PostgreSQL functions & triggers (rerunnable)

-- ==================== Updated At Helper ====================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_campsites_updated_at BEFORE UPDATE ON campsites
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_map_configurations_updated_at BEFORE UPDATE ON map_configurations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bundles_updated_at BEFORE UPDATE ON bundles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_revenue_reports_updated_at BEFORE UPDATE ON revenue_reports
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_seasonal_pricing_updated_at BEFORE UPDATE ON seasonal_pricing_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_dow_pricing_updated_at BEFORE UPDATE ON day_of_week_pricing
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_promo_codes_updated_at BEFORE UPDATE ON promo_codes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_prefs_updated_at BEFORE UPDATE ON notification_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_themes_updated_at BEFORE UPDATE ON themes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ==================== CQRS Sync ====================
CREATE OR REPLACE FUNCTION sync_booking_report()
RETURNS TRIGGER AS $$
DECLARE
    v_campsite_name VARCHAR(200);
    v_user_email VARCHAR(255);
    v_total_items INTEGER;
    v_has_products BOOLEAN;
    v_day_of_week INTEGER;
BEGIN
    SELECT name INTO v_campsite_name FROM campsites WHERE id = NEW.campsite_id;
    SELECT email INTO v_user_email FROM users WHERE id = NEW.user_id;

    SELECT COUNT(*), COALESCE(SUM(CASE WHEN product_type IN ('SALE', 'RENTAL_ITEM') THEN 1 ELSE 0 END) > 0, FALSE)
    INTO v_total_items, v_has_products
    FROM booking_items WHERE booking_id = NEW.id;

    v_day_of_week := EXTRACT(ISODOW FROM NEW.check_in_date);

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

CREATE TRIGGER trg_sync_booking_report
    AFTER INSERT OR UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION sync_booking_report();

-- ==================== Pricing Functions ====================
CREATE OR REPLACE FUNCTION calculate_product_price(
    p_product_id BIGINT,
    p_campsite_id BIGINT,
    p_date DATE
) RETURNS DECIMAL AS $$
DECLARE
    v_base_price DECIMAL(12, 2);
    v_adjusted_price DECIMAL(12, 2);
    v_adjustment RECORD;
BEGIN
    SELECT daily_rate INTO v_base_price FROM products WHERE id = p_product_id;
    v_adjusted_price := v_base_price;

    SELECT * INTO v_adjustment
    FROM seasonal_pricing_rules
    WHERE (product_id = p_product_id OR product_id IS NULL)
      AND campsite_id = p_campsite_id
      AND p_date BETWEEN start_date AND end_date
      AND is_active = TRUE
    ORDER BY priority DESC
    LIMIT 1;

    IF FOUND THEN
        IF v_adjustment.adjustment_type = 'PERCENTAGE' THEN
            v_adjusted_price := v_adjusted_price * (1 + v_adjustment.adjustment_value / 100);
        ELSE
            v_adjusted_price := v_adjusted_price + v_adjustment.adjustment_value;
        END IF;

        IF v_adjustment.min_price IS NOT NULL THEN
            v_adjusted_price := GREATEST(v_adjusted_price, v_adjustment.min_price);
        END IF;
        IF v_adjustment.max_price IS NOT NULL THEN
            v_adjusted_price := LEAST(v_adjusted_price, v_adjustment.max_price);
        END IF;
    END IF;

    RETURN v_adjusted_price;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validate_promo_code(
    p_code VARCHAR,
    p_user_id BIGINT,
    p_booking_amount DECIMAL
) RETURNS BOOLEAN AS $$
DECLARE
    v_promo promo_codes%ROWTYPE;
    v_usage_count INTEGER;
BEGIN
    SELECT * INTO v_promo FROM promo_codes WHERE code = p_code AND is_active = TRUE;
    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    IF v_promo.start_date IS NOT NULL AND NOW() < v_promo.start_date THEN
        RETURN FALSE;
    END IF;
    IF v_promo.end_date IS NOT NULL AND NOW() > v_promo.end_date THEN
        RETURN FALSE;
    END IF;
    IF v_promo.min_order_amount IS NOT NULL AND p_booking_amount < v_promo.min_order_amount THEN
        RETURN FALSE;
    END IF;
    IF v_promo.total_usage_limit IS NOT NULL AND v_promo.current_usage >= v_promo.total_usage_limit THEN
        RETURN FALSE;
    END IF;

    SELECT COUNT(*) INTO v_usage_count FROM promo_code_usages
    WHERE promo_code_id = v_promo.id AND user_id = p_user_id;

    IF v_promo.per_user_limit IS NOT NULL AND v_usage_count >= v_promo.per_user_limit THEN
        RETURN FALSE;
    END IF;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION increment_promo_usage()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE promo_codes
    SET current_usage = current_usage + 1
    WHERE id = NEW.promo_code_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_increment_promo_usage
    AFTER INSERT ON promo_code_usages
    FOR EACH ROW EXECUTE FUNCTION increment_promo_usage();

-- ==================== Notification Functions ====================
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

CREATE OR REPLACE FUNCTION create_booking_reminders()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'CONFIRMED' AND OLD.status != 'CONFIRMED' THEN
        INSERT INTO booking_reminders (booking_id, user_id, reminder_type, scheduled_at)
        VALUES (NEW.id, NEW.user_id, '24H', NEW.check_in_date - INTERVAL '1 day');

        INSERT INTO booking_reminders (booking_id, user_id, reminder_type, scheduled_at)
        VALUES (NEW.id, NEW.user_id, '3H', NEW.check_in_date - INTERVAL '3 hours');

        INSERT INTO booking_reminders (booking_id, user_id, reminder_type, scheduled_at)
        VALUES (NEW.id, NEW.user_id, '1H', NEW.check_in_date - INTERVAL '1 hour');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_booking_reminders
    AFTER UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION create_booking_reminders();

CREATE OR REPLACE FUNCTION mark_notifications_read(p_user_id BIGINT, p_notification_ids BIGINT[] DEFAULT NULL)
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    IF p_notification_ids IS NULL THEN
        UPDATE notifications
        SET is_read = TRUE, read_at = NOW()
        WHERE user_id = p_user_id AND is_read = FALSE;
    ELSE
        UPDATE notifications
        SET is_read = TRUE, read_at = NOW()
        WHERE user_id = p_user_id AND id = ANY(p_notification_ids) AND is_read = FALSE;
    END IF;

    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- ==================== Auth Utility Functions ====================
CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS void AS $$
BEGIN
    DELETE FROM refresh_tokens WHERE expires_at < NOW() - INTERVAL '7 days';
    DELETE FROM email_verification_tokens WHERE expires_at < NOW() - INTERVAL '1 day';
    DELETE FROM password_reset_tokens WHERE expires_at < NOW() - INTERVAL '1 day';
    DELETE FROM failed_login_attempts WHERE attempt_time < NOW() - INTERVAL '30 days';
    DELETE FROM user_sessions WHERE is_active = FALSE AND logged_out_at < NOW() - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION is_user_locked_out(p_email VARCHAR, p_lockout_threshold INTEGER DEFAULT 5, p_lockout_window_minutes INTEGER DEFAULT 15)
RETURNS BOOLEAN AS $$
DECLARE
    v_failed_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_failed_count
    FROM failed_login_attempts
    WHERE email = p_email
    AND attempt_time > NOW() - (p_lockout_window_minutes || ' minutes')::INTERVAL;

    RETURN v_failed_count >= p_lockout_threshold;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION revoke_all_user_tokens(p_user_id BIGINT)
RETURNS void AS $$
BEGIN
    UPDATE refresh_tokens
    SET revoked = TRUE, revoked_at = NOW()
    WHERE user_id = p_user_id AND revoked = FALSE;

    UPDATE user_sessions
    SET is_active = FALSE, logged_out_at = NOW()
    WHERE user_id = p_user_id AND is_active = TRUE;
END;
$$ LANGUAGE plpgsql;

-- ==================== Payments Functions ====================
CREATE SEQUENCE IF NOT EXISTS invoice_number_seq START 1;

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

CREATE OR REPLACE FUNCTION reset_daily_invoice_sequence()
RETURNS void AS $$
BEGIN
    ALTER SEQUENCE invoice_number_seq RESTART WITH 1;
END;
$$ LANGUAGE plpgsql;

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

        UPDATE inventory_locks
        SET lock_type = 'CONFIRMED',
            expires_at = (SELECT check_out_date + INTERVAL '1 day' FROM bookings WHERE id = NEW.booking_id)
        WHERE booking_id = NEW.booking_id AND lock_type = 'CART';
    ELSIF NEW.status = 'FAILED' AND OLD.status NOT IN ('FAILED', 'EXPIRED', 'CANCELLED') THEN
        DELETE FROM inventory_locks WHERE booking_id = NEW.booking_id AND lock_type = 'CART';
    ELSIF NEW.status = 'EXPIRED' AND OLD.status NOT IN ('FAILED', 'EXPIRED', 'CANCELLED') THEN
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
