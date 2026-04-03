-- Update booking report sync to treat rental items as products
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
    SELECT COUNT(*), COALESCE(SUM(CASE WHEN product_type IN ('SALE', 'RENTAL_ITEM') THEN 1 ELSE 0 END) > 0, FALSE)
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
