-- V5: Pricing and Promotions Tables for Spot Camp ID Backend
-- Purpose: Dynamic pricing, seasonal rules, and promotional offers
-- Database: PostgreSQL

-- ==================== Seasonal Pricing Rules ====================
-- Define seasonal price adjustments for products
CREATE TABLE seasonal_pricing_rules (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    product_id BIGINT,  -- NULL applies to all products in campsite
    name VARCHAR(200) NOT NULL,
    description TEXT,

    -- Date range
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    -- Price adjustment
    adjustment_type VARCHAR(20) NOT NULL,  -- PERCENTAGE, FIXED_AMOUNT
    adjustment_value DECIMAL(10, 2) NOT NULL,  -- +20 for 20% increase, -15 for 15% discount
    min_price DECIMAL(12, 2),  -- Floor price (won't go below this)
    max_price DECIMAL(12, 2),  -- Ceiling price (won't go above this)

    -- Priority (higher = takes precedence)
    priority INTEGER NOT NULL DEFAULT 0,

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_seasonal_pricing_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE,
    CONSTRAINT fk_seasonal_pricing_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_adjustment_type CHECK (adjustment_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT chk_date_range CHECK (end_date >= start_date),
    CONSTRAINT chk_min_max_price CHECK (max_price IS NULL OR min_price IS NULL OR max_price >= min_price)
);

CREATE INDEX idx_seasonal_pricing_campsite ON seasonal_pricing_rules(campsite_id);
CREATE INDEX idx_seasonal_pricing_product ON seasonal_pricing_rules(product_id);
CREATE INDEX idx_seasonal_pricing_dates ON seasonal_pricing_rules(start_date, end_date);
CREATE INDEX idx_seasonal_pricing_active ON seasonal_pricing_rules(campsite_id, is_active, start_date, end_date);

-- ==================== Day of Week Pricing ====================
-- Different prices for weekdays vs weekends
CREATE TABLE day_of_week_pricing (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    product_id BIGINT,  -- NULL applies to all products
    day_of_week INTEGER NOT NULL,  -- 1=Monday, 7=Sunday (ISO)

    -- Price adjustment
    adjustment_type VARCHAR(20) NOT NULL,
    adjustment_value DECIMAL(10, 2) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dow_pricing_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE,
    CONSTRAINT fk_dow_pricing_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_dow_adjustment_type CHECK (adjustment_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT uq_dow_pricing UNIQUE (campsite_id, product_id, day_of_week)
);

CREATE INDEX idx_dow_pricing_campsite ON day_of_week_pricing(campsite_id);

-- ==================== Special Date Pricing ====================
-- Specific date overrides (holidays, events)
CREATE TABLE special_date_pricing (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    product_id BIGINT,  -- NULL applies to all products
    special_date DATE NOT NULL,
    name VARCHAR(200) NOT NULL,  -- e.g., "New Year's Eve", "Eid al-Fitr"

    -- Price adjustment
    adjustment_type VARCHAR(20) NOT NULL,
    adjustment_value DECIMAL(10, 2) NOT NULL,

    -- Override base price completely (optional)
    override_price DECIMAL(12, 2),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_special_date_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE,
    CONSTRAINT fk_special_date_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_special_adjustment_type CHECK (adjustment_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT uq_special_date UNIQUE (campsite_id, product_id, special_date)
);

CREATE INDEX idx_special_date_campsite ON special_date_pricing(campsite_id);
CREATE INDEX idx_special_date ON special_date_pricing(special_date);

-- ==================== Promotional Codes ====================
-- Discount codes for marketing campaigns
CREATE TABLE promo_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,

    -- Scope
    campsite_id BIGINT,  -- NULL = platform-wide
    product_id BIGINT,   -- NULL = all products

    -- Discount
    discount_type VARCHAR(20) NOT NULL,  -- PERCENTAGE, FIXED_AMOUNT
    discount_value DECIMAL(10, 2) NOT NULL,
    max_discount_amount DECIMAL(12, 2),  -- Cap for percentage discounts
    min_order_amount DECIMAL(12, 2),     -- Minimum order to apply

    -- Usage limits
    total_usage_limit INTEGER,  -- NULL = unlimited
    per_user_limit INTEGER DEFAULT 1,
    current_usage INTEGER NOT NULL DEFAULT 0,

    -- Validity
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Restrictions
    first_booking_only BOOLEAN NOT NULL DEFAULT FALSE,
    applicable_days INTEGER[],  -- Array of days [1,2,3,4,5,6,7], NULL = all days

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    CONSTRAINT fk_promo_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE,
    CONSTRAINT fk_promo_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    CONSTRAINT chk_promo_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT chk_promo_dates CHECK (end_date > start_date),
    CONSTRAINT chk_promo_discount_value CHECK (discount_value > 0)
);

CREATE INDEX idx_promo_code ON promo_codes(code);
CREATE INDEX idx_promo_campsite ON promo_codes(campsite_id);
CREATE INDEX idx_promo_active ON promo_codes(is_active, start_date, end_date);
CREATE INDEX idx_promo_dates ON promo_codes(start_date, end_date);

-- ==================== Promo Code Usage ====================
-- Track usage of promo codes
CREATE TABLE promo_code_usages (
    id BIGSERIAL PRIMARY KEY,
    promo_code_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    discount_amount DECIMAL(12, 2) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_promo_usage_code FOREIGN KEY (promo_code_id) REFERENCES promo_codes(id),
    CONSTRAINT fk_promo_usage_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_promo_usage_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE INDEX idx_promo_usage_code ON promo_code_usages(promo_code_id);
CREATE INDEX idx_promo_usage_user ON promo_code_usages(user_id);
CREATE INDEX idx_promo_usage_booking ON promo_code_usages(booking_id);
CREATE INDEX idx_promo_usage_user_code ON promo_code_usages(user_id, promo_code_id);

-- ==================== Price Calculation Function ====================
-- Calculate final price for a product on a specific date
CREATE OR REPLACE FUNCTION calculate_product_price(
    p_product_id BIGINT,
    p_campsite_id BIGINT,
    p_date DATE
)
RETURNS DECIMAL(12, 2) AS $$
DECLARE
    v_base_price DECIMAL(12, 2);
    v_final_price DECIMAL(12, 2);
    v_adjustment RECORD;
    v_day_of_week INTEGER;
BEGIN
    -- Get base price
    SELECT COALESCE(daily_rate, unit_price, base_price) INTO v_base_price
    FROM products WHERE id = p_product_id;

    IF v_base_price IS NULL THEN
        RETURN NULL;
    END IF;

    v_final_price := v_base_price;
    v_day_of_week := EXTRACT(ISODOW FROM p_date);

    -- 1. Apply special date pricing (highest priority)
    SELECT * INTO v_adjustment
    FROM special_date_pricing
    WHERE campsite_id = p_campsite_id
    AND (product_id = p_product_id OR product_id IS NULL)
    AND special_date = p_date
    AND is_active = TRUE
    ORDER BY product_id NULLS LAST
    LIMIT 1;

    IF FOUND THEN
        IF v_adjustment.override_price IS NOT NULL THEN
            RETURN v_adjustment.override_price;
        ELSIF v_adjustment.adjustment_type = 'PERCENTAGE' THEN
            v_final_price := v_final_price * (1 + v_adjustment.adjustment_value / 100);
        ELSE
            v_final_price := v_final_price + v_adjustment.adjustment_value;
        END IF;
        RETURN GREATEST(v_final_price, 0);
    END IF;

    -- 2. Apply seasonal pricing
    SELECT * INTO v_adjustment
    FROM seasonal_pricing_rules
    WHERE campsite_id = p_campsite_id
    AND (product_id = p_product_id OR product_id IS NULL)
    AND p_date BETWEEN start_date AND end_date
    AND is_active = TRUE
    ORDER BY priority DESC, product_id NULLS LAST
    LIMIT 1;

    IF FOUND THEN
        IF v_adjustment.adjustment_type = 'PERCENTAGE' THEN
            v_final_price := v_final_price * (1 + v_adjustment.adjustment_value / 100);
        ELSE
            v_final_price := v_final_price + v_adjustment.adjustment_value;
        END IF;

        -- Apply min/max constraints
        IF v_adjustment.min_price IS NOT NULL THEN
            v_final_price := GREATEST(v_final_price, v_adjustment.min_price);
        END IF;
        IF v_adjustment.max_price IS NOT NULL THEN
            v_final_price := LEAST(v_final_price, v_adjustment.max_price);
        END IF;
    END IF;

    -- 3. Apply day of week pricing
    SELECT * INTO v_adjustment
    FROM day_of_week_pricing
    WHERE campsite_id = p_campsite_id
    AND (product_id = p_product_id OR product_id IS NULL)
    AND day_of_week = v_day_of_week
    AND is_active = TRUE
    ORDER BY product_id NULLS LAST
    LIMIT 1;

    IF FOUND THEN
        IF v_adjustment.adjustment_type = 'PERCENTAGE' THEN
            v_final_price := v_final_price * (1 + v_adjustment.adjustment_value / 100);
        ELSE
            v_final_price := v_final_price + v_adjustment.adjustment_value;
        END IF;
    END IF;

    RETURN GREATEST(ROUND(v_final_price, 2), 0);
END;
$$ LANGUAGE plpgsql;

-- ==================== Promo Code Validation Function ====================
CREATE OR REPLACE FUNCTION validate_promo_code(
    p_code VARCHAR,
    p_user_id BIGINT,
    p_campsite_id BIGINT,
    p_order_amount DECIMAL
)
RETURNS TABLE(
    is_valid BOOLEAN,
    promo_id BIGINT,
    discount_amount DECIMAL,
    error_message VARCHAR
) AS $$
DECLARE
    v_promo RECORD;
    v_user_usage INTEGER;
    v_discount DECIMAL;
BEGIN
    -- Find promo code
    SELECT * INTO v_promo
    FROM promo_codes
    WHERE code = UPPER(p_code);

    IF NOT FOUND THEN
        RETURN QUERY SELECT FALSE, NULL::BIGINT, 0::DECIMAL, 'Invalid promo code'::VARCHAR;
        RETURN;
    END IF;

    -- Check if active
    IF NOT v_promo.is_active THEN
        RETURN QUERY SELECT FALSE, NULL::BIGINT, 0::DECIMAL, 'Promo code is no longer active'::VARCHAR;
        RETURN;
    END IF;

    -- Check date validity
    IF NOW() < v_promo.start_date OR NOW() > v_promo.end_date THEN
        RETURN QUERY SELECT FALSE, NULL::BIGINT, 0::DECIMAL, 'Promo code has expired or not yet valid'::VARCHAR;
        RETURN;
    END IF;

    -- Check campsite restriction
    IF v_promo.campsite_id IS NOT NULL AND v_promo.campsite_id != p_campsite_id THEN
        RETURN QUERY SELECT FALSE, NULL::BIGINT, 0::DECIMAL, 'Promo code not valid for this campsite'::VARCHAR;
        RETURN;
    END IF;

    -- Check total usage limit
    IF v_promo.total_usage_limit IS NOT NULL AND v_promo.current_usage >= v_promo.total_usage_limit THEN
        RETURN QUERY SELECT FALSE, NULL::BIGINT, 0::DECIMAL, 'Promo code usage limit reached'::VARCHAR;
        RETURN;
    END IF;

    -- Check per-user limit
    SELECT COUNT(*) INTO v_user_usage
    FROM promo_code_usages
    WHERE promo_code_id = v_promo.id AND user_id = p_user_id;

    IF v_promo.per_user_limit IS NOT NULL AND v_user_usage >= v_promo.per_user_limit THEN
        RETURN QUERY SELECT FALSE, NULL::BIGINT, 0::DECIMAL, 'You have already used this promo code'::VARCHAR;
        RETURN;
    END IF;

    -- Check minimum order
    IF v_promo.min_order_amount IS NOT NULL AND p_order_amount < v_promo.min_order_amount THEN
        RETURN QUERY SELECT FALSE, NULL::BIGINT, 0::DECIMAL,
            ('Minimum order amount is ' || v_promo.min_order_amount)::VARCHAR;
        RETURN;
    END IF;

    -- Check first booking only
    IF v_promo.first_booking_only THEN
        IF EXISTS (SELECT 1 FROM bookings WHERE user_id = p_user_id AND status = 'CONFIRMED') THEN
            RETURN QUERY SELECT FALSE, NULL::BIGINT, 0::DECIMAL, 'Promo code is for first booking only'::VARCHAR;
            RETURN;
        END IF;
    END IF;

    -- Calculate discount
    IF v_promo.discount_type = 'PERCENTAGE' THEN
        v_discount := p_order_amount * v_promo.discount_value / 100;
        IF v_promo.max_discount_amount IS NOT NULL THEN
            v_discount := LEAST(v_discount, v_promo.max_discount_amount);
        END IF;
    ELSE
        v_discount := LEAST(v_promo.discount_value, p_order_amount);
    END IF;

    RETURN QUERY SELECT TRUE, v_promo.id, ROUND(v_discount, 2), NULL::VARCHAR;
END;
$$ LANGUAGE plpgsql;

-- ==================== Triggers ====================

CREATE TRIGGER update_seasonal_pricing_updated_at
    BEFORE UPDATE ON seasonal_pricing_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_dow_pricing_updated_at
    BEFORE UPDATE ON day_of_week_pricing
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_promo_codes_updated_at
    BEFORE UPDATE ON promo_codes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger to increment promo code usage
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
