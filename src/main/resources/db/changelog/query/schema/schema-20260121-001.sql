-- Consolidated schema for Spot Camp ID (Liquibase)
-- Target: PostgreSQL (with H2 running in PostgreSQL mode for local)

-- ==================== Users & Authentication ====================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'CAMPER',
    business_name VARCHAR(200),
    business_code VARCHAR(20),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_role CHECK (role IN (
        'CAMPER',
        'MERCHANT',
        'MERCHANT_ADMIN',
        'MERCHANT_MEMBER',
        'SUPERADMIN',
        'ADMIN'
    )),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT chk_business_name CHECK (
        (role IN ('MERCHANT', 'MERCHANT_ADMIN', 'MERCHANT_MEMBER') AND business_name IS NOT NULL) OR
        (role NOT IN ('MERCHANT', 'MERCHANT_ADMIN', 'MERCHANT_MEMBER'))
    )
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_business_code ON users(business_code);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE email_verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    location VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logged_out_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE failed_login_attempts (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    attempt_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    failure_reason VARCHAR(50) NOT NULL,

    CONSTRAINT chk_failure_reason CHECK (failure_reason IN ('INVALID_PASSWORD', 'USER_NOT_FOUND', 'ACCOUNT_LOCKED', 'ACCOUNT_SUSPENDED', 'EMAIL_NOT_VERIFIED'))
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_active ON refresh_tokens(user_id, revoked, expires_at);

CREATE INDEX idx_email_verification_token ON email_verification_tokens(token);
CREATE INDEX idx_email_verification_user ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_expires ON email_verification_tokens(expires_at);

CREATE INDEX idx_password_reset_token ON password_reset_tokens(token_hash);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at);

CREATE INDEX idx_user_sessions_user ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_session ON user_sessions(session_id);
CREATE INDEX idx_user_sessions_active ON user_sessions(user_id, is_active);
CREATE INDEX idx_user_sessions_last_activity ON user_sessions(last_activity_at);

CREATE INDEX idx_failed_login_email ON failed_login_attempts(email);
CREATE INDEX idx_failed_login_ip ON failed_login_attempts(ip_address);
CREATE INDEX idx_failed_login_time ON failed_login_attempts(attempt_time);
CREATE INDEX idx_failed_login_email_time ON failed_login_attempts(email, attempt_time);
CREATE INDEX idx_failed_login_ip_time ON failed_login_attempts(ip_address, attempt_time);

-- ==================== Campsites ====================
CREATE TABLE campsites (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    full_description TEXT,
    location VARCHAR(500) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    cover_image_url VARCHAR(500),
    check_in_time TIME NOT NULL DEFAULT '14:00',
    check_out_time TIME NOT NULL DEFAULT '12:00',
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    min_price DECIMAL(12, 2),
    rating DECIMAL(3, 2) DEFAULT 0.0,
    review_count INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_campsites_merchant FOREIGN KEY (merchant_id) REFERENCES users(id),
    CONSTRAINT chk_rating CHECK (rating >= 0.0 AND rating <= 5.0),
    CONSTRAINT chk_campsite_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT chk_coordinates CHECK (
        (latitude IS NULL AND longitude IS NULL) OR
        (latitude IS NOT NULL AND longitude IS NOT NULL)
    )
);

CREATE INDEX idx_campsites_merchant ON campsites(merchant_id);
CREATE INDEX idx_campsites_status ON campsites(status);
CREATE INDEX idx_campsites_location ON campsites(latitude, longitude);
CREATE UNIQUE INDEX idx_campsites_code ON campsites(code);

-- ==================== Campsite Images & Amenities ====================
CREATE TABLE campsite_images (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    caption VARCHAR(200),
    display_order INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_campsite_images_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE
);

CREATE TABLE amenities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    icon VARCHAR(50),
    category VARCHAR(50) NOT NULL
);

CREATE TABLE campsite_amenities (
    campsite_id BIGINT NOT NULL,
    amenity_id BIGINT NOT NULL,

    PRIMARY KEY (campsite_id, amenity_id),
    CONSTRAINT fk_campsite_amenities_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE,
    CONSTRAINT fk_campsite_amenities_amenity FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE
);

CREATE TABLE campsite_rules (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    rule_text VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_campsite_rules_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE
);

-- ==================== Products (Inventory) ====================
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    base_price DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    category VARCHAR(20) NOT NULL,
    item_type VARCHAR(20) NOT NULL,

    -- Rental specific fields
    stock_total INTEGER,
    buffer_time_minutes INTEGER DEFAULT 120,
    daily_rate DECIMAL(12, 2),

    -- Sale specific fields
    current_stock INTEGER,
    reorder_level INTEGER DEFAULT 10,
    unit_price DECIMAL(12, 2),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_products_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id),
    CONSTRAINT chk_product_type CHECK (type IN ('RENTAL_SPOT', 'RENTAL_ITEM', 'SALE')),
    CONSTRAINT chk_product_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_base_price CHECK (base_price >= 0),

    -- Rental constraints
    CONSTRAINT chk_rental_fields CHECK (
        (type IN ('RENTAL_SPOT', 'RENTAL_ITEM') AND stock_total IS NOT NULL AND daily_rate IS NOT NULL) OR
        (type NOT IN ('RENTAL_SPOT', 'RENTAL_ITEM') AND stock_total IS NULL AND daily_rate IS NULL)
    ),

    -- Sale constraints
    CONSTRAINT chk_sale_fields CHECK (
        (type = 'SALE' AND current_stock IS NOT NULL AND unit_price IS NOT NULL) OR
        (type != 'SALE' AND current_stock IS NULL AND unit_price IS NULL)
    )
);

CREATE INDEX idx_products_campsite ON products(campsite_id);
CREATE INDEX idx_products_type ON products(type);
CREATE INDEX idx_products_status ON products(status);

CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ==================== Visual Map Configuration ====================
CREATE TABLE map_configurations (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    background_image_url VARCHAR(500) NOT NULL,
    image_width INTEGER NOT NULL,
    image_height INTEGER NOT NULL,
    map_name VARCHAR(200) NOT NULL,
    map_code VARCHAR(20) NOT NULL,
    config_data JSONB NOT NULL,
    version_number INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_map_config_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id),
    CONSTRAINT chk_map_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_image_dimensions CHECK (image_width > 0 AND image_height > 0)
);

CREATE INDEX idx_map_config_campsite ON map_configurations(campsite_id);
CREATE INDEX idx_map_config_status ON map_configurations(status);
CREATE INDEX idx_map_config_campsite_name ON map_configurations(campsite_id, map_name);
CREATE INDEX idx_map_config_campsite_code ON map_configurations(campsite_id, map_code);

-- ==================== Bundles ====================
CREATE TABLE bundles (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    bundle_price DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_bundles_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id),
    CONSTRAINT chk_bundle_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_bundle_price CHECK (bundle_price >= 0)
);

CREATE TABLE bundle_components (
    id BIGSERIAL PRIMARY KEY,
    bundle_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_bundle_components_bundle FOREIGN KEY (bundle_id) REFERENCES bundles(id) ON DELETE CASCADE,
    CONSTRAINT fk_bundle_components_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_quantity CHECK (quantity > 0)
);

-- ==================== Bookings & Cart ====================
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    campsite_id BIGINT NOT NULL,
    spot_id VARCHAR(100),
    spot_name VARCHAR(200),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_CART',
    total_amount DECIMAL(12, 2) NOT NULL,

    -- Payment info
    payment_method VARCHAR(50),
    payment_reference VARCHAR(200),
    invoice_number VARCHAR(100),

    -- Contact info
    contact_name VARCHAR(200),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    special_requests TEXT,

    -- Timestamps
    expires_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id),
    CONSTRAINT chk_booking_status CHECK (status IN ('IN_CART', 'PAYMENT_PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_booking_dates CHECK (check_out_date > check_in_date)
);

CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_campsite ON bookings(campsite_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_bookings_spot ON bookings(campsite_id, spot_id);

CREATE TABLE booking_items (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(200),
    product_type VARCHAR(20),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(12, 2) NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,

    CONSTRAINT fk_booking_items_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

CREATE TABLE inventory_locks (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    spot_id VARCHAR(100),
    quantity INTEGER NOT NULL DEFAULT 1,
    lock_type VARCHAR(20) NOT NULL,
    start_date DATE,
    end_date DATE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_locks_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inventory_locks_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT chk_inventory_dates CHECK (
        (start_date IS NULL AND end_date IS NULL) OR
        (start_date IS NOT NULL AND end_date IS NOT NULL AND end_date > start_date)
    )
);

CREATE INDEX idx_inventory_locks_product ON inventory_locks(product_id);
CREATE INDEX idx_inventory_locks_booking ON inventory_locks(booking_id);
CREATE INDEX idx_inventory_locks_spot ON inventory_locks(spot_id);
CREATE INDEX idx_inventory_locks_dates ON inventory_locks(start_date, end_date);
CREATE INDEX idx_inventory_locks_expires ON inventory_locks(expires_at);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    campsite_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT,
    images TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_reviews_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_review_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT chk_review_status CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED'))
);

CREATE UNIQUE INDEX idx_reviews_booking ON reviews(booking_id);
CREATE INDEX idx_reviews_campsite ON reviews(campsite_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);

-- ==================== Themes ====================
CREATE TABLE themes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    tokens_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

-- ==================== Pricing ====================
CREATE TABLE seasonal_pricing_rules (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    product_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    adjustment_type VARCHAR(20) NOT NULL,
    adjustment_value DECIMAL(10, 2) NOT NULL,
    min_price DECIMAL(12, 2),
    max_price DECIMAL(12, 2),
    priority INTEGER NOT NULL DEFAULT 0,
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

CREATE TABLE day_of_week_pricing (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    product_id BIGINT,
    day_of_week INTEGER NOT NULL,
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

CREATE TABLE special_date_pricing (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    product_id BIGINT,
    special_date DATE NOT NULL,
    name VARCHAR(200) NOT NULL,
    adjustment_type VARCHAR(20) NOT NULL,
    adjustment_value DECIMAL(10, 2) NOT NULL,
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

CREATE TABLE promo_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    campsite_id BIGINT,
    product_id BIGINT,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    max_discount_amount DECIMAL(12, 2),
    min_order_amount DECIMAL(12, 2),
    total_usage_limit INTEGER,
    per_user_limit INTEGER DEFAULT 1,
    current_usage INTEGER NOT NULL DEFAULT 0,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_promo_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE SET NULL,
    CONSTRAINT fk_promo_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    CONSTRAINT chk_promo_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT chk_promo_discount_value CHECK (discount_value > 0),
    CONSTRAINT chk_promo_min_amount CHECK (min_order_amount IS NULL OR min_order_amount >= 0),
    CONSTRAINT chk_promo_max_amount CHECK (max_discount_amount IS NULL OR max_discount_amount >= 0),
    CONSTRAINT chk_promo_limits CHECK (total_usage_limit IS NULL OR total_usage_limit >= 0)
);

CREATE INDEX idx_promo_code ON promo_codes(code);
CREATE INDEX idx_promo_campsite ON promo_codes(campsite_id);
CREATE INDEX idx_promo_active ON promo_codes(is_active, start_date, end_date);
CREATE INDEX idx_promo_dates ON promo_codes(start_date, end_date);

CREATE TABLE promo_code_usages (
    id BIGSERIAL PRIMARY KEY,
    promo_code_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    booking_id BIGINT,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_promo_usage_code FOREIGN KEY (promo_code_id) REFERENCES promo_codes(id) ON DELETE CASCADE,
    CONSTRAINT fk_promo_usage_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_promo_usage_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL
);

CREATE INDEX idx_promo_usage_code ON promo_code_usages(promo_code_id);
CREATE INDEX idx_promo_usage_user ON promo_code_usages(user_id);
CREATE INDEX idx_promo_usage_booking ON promo_code_usages(booking_id);
CREATE INDEX idx_promo_usage_user_code ON promo_code_usages(user_id, promo_code_id);

-- ==================== Notifications ====================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,

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
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created ON notifications(created_at);
CREATE INDEX idx_notifications_reference ON notifications(reference_type, reference_id);

CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email_booking_confirmation BOOLEAN NOT NULL DEFAULT TRUE,
    email_payment_receipt BOOLEAN NOT NULL DEFAULT TRUE,
    email_booking_reminder BOOLEAN NOT NULL DEFAULT TRUE,
    email_review_request BOOLEAN NOT NULL DEFAULT TRUE,
    email_promotions BOOLEAN NOT NULL DEFAULT TRUE,
    email_newsletter BOOLEAN NOT NULL DEFAULT FALSE,
    push_booking_updates BOOLEAN NOT NULL DEFAULT TRUE,
    push_payment_updates BOOLEAN NOT NULL DEFAULT TRUE,
    push_promotions BOOLEAN NOT NULL DEFAULT FALSE,
    sms_booking_confirmation BOOLEAN NOT NULL DEFAULT FALSE,
    sms_payment_confirmation BOOLEAN NOT NULL DEFAULT FALSE,
    merchant_new_booking_email BOOLEAN NOT NULL DEFAULT TRUE,
    merchant_new_booking_push BOOLEAN NOT NULL DEFAULT TRUE,
    merchant_daily_summary BOOLEAN NOT NULL DEFAULT TRUE,
    merchant_low_stock_alert BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_prefs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_prefs_user ON notification_preferences(user_id);

CREATE TABLE email_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    template_id VARCHAR(100) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    status_message TEXT,
    template_data JSONB,
    html_content TEXT,
    provider VARCHAR(50) DEFAULT 'SENDGRID',
    provider_message_id VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    bounced_at TIMESTAMP,
    failed_at TIMESTAMP,
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

CREATE TABLE push_notification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    device_type VARCHAR(20) NOT NULL,
    device_name VARCHAR(200),
    device_id VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_push_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_device_type CHECK (device_type IN ('IOS', 'ANDROID', 'WEB')),
    CONSTRAINT uq_push_token UNIQUE (user_id, token)
);

CREATE INDEX idx_push_tokens_user ON push_notification_tokens(user_id);
CREATE INDEX idx_push_tokens_active ON push_notification_tokens(user_id, is_active);

CREATE TABLE push_notification_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_id BIGINT,
    notification_id BIGINT,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    data JSONB,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    status_message TEXT,
    provider VARCHAR(50) DEFAULT 'FCM',
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

CREATE TABLE system_announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    target_audience VARCHAR(30) NOT NULL DEFAULT 'ALL',
    display_from TIMESTAMP NOT NULL,
    display_until TIMESTAMP NOT NULL,
    is_dismissible BOOLEAN NOT NULL DEFAULT TRUE,
    priority INTEGER NOT NULL DEFAULT 0,
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

CREATE TABLE dismissed_announcements (
    user_id BIGINT NOT NULL,
    announcement_id BIGINT NOT NULL,
    dismissed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, announcement_id),
    CONSTRAINT fk_dismissed_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_dismissed_announcement FOREIGN KEY (announcement_id) REFERENCES system_announcements(id) ON DELETE CASCADE
);

CREATE TABLE booking_reminders (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reminder_type VARCHAR(20) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reminder_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_reminder_type CHECK (reminder_type IN ('24H', '3H', '1H', 'IMMEDIATE'))
);

CREATE INDEX idx_reminders_scheduled ON booking_reminders(scheduled_at, sent);
CREATE INDEX idx_reminders_booking ON booking_reminders(booking_id);
CREATE INDEX idx_reminders_user ON booking_reminders(user_id);

-- ==================== Payments ====================
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    campsite_id BIGINT NOT NULL,
    transaction_ref VARCHAR(100) NOT NULL UNIQUE,
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    external_ref VARCHAR(200),
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    fee_amount DECIMAL(12, 2) DEFAULT 0,
    net_amount DECIMAL(12, 2),
    payment_method VARCHAR(50) NOT NULL,
    payment_channel VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    status_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    paid_at TIMESTAMP,
    failed_at TIMESTAMP,
    refunded_at TIMESTAMP,
    doku_request_id VARCHAR(200),
    doku_response_code VARCHAR(50),
    doku_va_number VARCHAR(50),
    doku_qris_string TEXT,
    request_payload JSONB,
    response_payload JSONB,
    webhook_payload JSONB,
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

CREATE TABLE payment_refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_transaction_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    refund_ref VARCHAR(100) NOT NULL UNIQUE,
    external_refund_ref VARCHAR(200),
    refund_amount DECIMAL(12, 2) NOT NULL,
    original_amount DECIMAL(12, 2) NOT NULL,
    refund_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    status_message VARCHAR(500),
    reason VARCHAR(50) NOT NULL,
    reason_detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    request_payload JSONB,
    response_payload JSONB,
    initiated_by BIGINT,

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

CREATE TABLE payment_webhook_logs (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    transaction_ref VARCHAR(100),
    payload JSONB NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP,

    CONSTRAINT chk_webhook_processed CHECK (
        (processed = TRUE AND processed_at IS NOT NULL) OR
        (processed = FALSE)
    )
);

CREATE INDEX idx_webhook_log_provider ON payment_webhook_logs(provider);
CREATE INDEX idx_webhook_log_event ON payment_webhook_logs(event_type);
CREATE INDEX idx_webhook_log_transaction ON payment_webhook_logs(transaction_ref);
CREATE INDEX idx_webhook_log_received ON payment_webhook_logs(received_at);
CREATE INDEX idx_webhook_log_processed ON payment_webhook_logs(processed);

CREATE TABLE merchant_payouts (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    campsite_id BIGINT,
    payout_ref VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    processed_at TIMESTAMP,
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    account_holder VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payout_merchant FOREIGN KEY (merchant_id) REFERENCES users(id),
    CONSTRAINT fk_payout_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id)
);

CREATE INDEX idx_payout_merchant ON merchant_payouts(merchant_id);
CREATE INDEX idx_payout_campsite ON merchant_payouts(campsite_id);
CREATE INDEX idx_payout_status ON merchant_payouts(status);
CREATE INDEX idx_payout_period ON merchant_payouts(period_start, period_end);
CREATE INDEX idx_payout_created ON merchant_payouts(created_at);

CREATE TABLE payout_items (
    id BIGSERIAL PRIMARY KEY,
    payout_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    commission_amount DECIMAL(12, 2) NOT NULL,
    payment_transaction_id BIGINT,

    CONSTRAINT fk_payout_item_payout FOREIGN KEY (payout_id) REFERENCES merchant_payouts(id) ON DELETE CASCADE,
    CONSTRAINT fk_payout_item_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_payout_item_payment FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id)
);

CREATE INDEX idx_payout_item_payout ON payout_items(payout_id);
CREATE INDEX idx_payout_item_booking ON payout_items(booking_id);

-- ==================== Reports (CQRS) ====================
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
    booking_month VARCHAR(7) NOT NULL,
    booking_year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    total_items INTEGER DEFAULT 0 NOT NULL,
    has_products BOOLEAN DEFAULT FALSE NOT NULL,
    revenue_month VARCHAR(7) NOT NULL,
    is_weekend_booking BOOLEAN DEFAULT FALSE NOT NULL,
    advance_days INTEGER DEFAULT 0 NOT NULL,
    season VARCHAR(20),

    CONSTRAINT fk_booking_report_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id),
    CONSTRAINT fk_booking_report_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_booking_report_campsite_date ON booking_reports(campsite_id, booking_date);
CREATE INDEX idx_booking_report_status ON booking_reports(status);
CREATE INDEX idx_booking_report_created ON booking_reports(created_at);
CREATE INDEX idx_booking_report_month ON booking_reports(booking_month);
CREATE INDEX idx_booking_report_year ON booking_reports(booking_year);
CREATE INDEX idx_booking_report_user ON booking_reports(user_id);
CREATE INDEX idx_booking_report_revenue_month ON booking_reports(revenue_month);
CREATE INDEX idx_booking_report_season ON booking_reports(season);

CREATE TABLE revenue_reports (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT,
    campsite_name VARCHAR(200),
    period_type VARCHAR(20) NOT NULL,
    period_key VARCHAR(20) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_revenue DECIMAL(12, 2) DEFAULT 0 NOT NULL,
    spot_revenue DECIMAL(12, 2) DEFAULT 0 NOT NULL,
    product_revenue DECIMAL(12, 2) DEFAULT 0 NOT NULL,
    total_bookings INTEGER DEFAULT 0 NOT NULL,
    confirmed_bookings INTEGER DEFAULT 0 NOT NULL,
    cancelled_bookings INTEGER DEFAULT 0 NOT NULL,
    total_nights INTEGER DEFAULT 0 NOT NULL,
    average_booking_value DECIMAL(8, 2),
    occupancy_rate DECIMAL(5, 2),
    unique_customers INTEGER DEFAULT 0 NOT NULL,
    new_customers INTEGER DEFAULT 0 NOT NULL,
    returning_customers INTEGER DEFAULT 0 NOT NULL,
    weekend_bookings INTEGER DEFAULT 0 NOT NULL,
    weekday_bookings INTEGER DEFAULT 0 NOT NULL,
    weekend_revenue DECIMAL(12, 2) DEFAULT 0,
    weekday_revenue DECIMAL(12, 2) DEFAULT 0,
    advance_booking_days DECIMAL(6, 2),
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_revenue_campsite_period ON revenue_reports(campsite_id, period_type, period_key);
CREATE INDEX idx_revenue_period ON revenue_reports(period_type, period_key);
CREATE INDEX idx_revenue_period_start ON revenue_reports(period_start);
CREATE INDEX idx_revenue_last_updated ON revenue_reports(last_updated);

CREATE TABLE spot_availability (
    id BIGSERIAL PRIMARY KEY,
    campsite_id BIGINT NOT NULL,
    spot_id VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    booking_id BIGINT,
    price_override DECIMAL(12, 2),
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_spot_availability_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id) ON DELETE CASCADE,
    CONSTRAINT fk_spot_availability_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    CONSTRAINT uq_spot_availability UNIQUE (campsite_id, spot_id, date)
);

CREATE INDEX idx_spot_availability_campsite ON spot_availability(campsite_id);
CREATE INDEX idx_spot_availability_date ON spot_availability(date);
CREATE INDEX idx_spot_availability_available ON spot_availability(campsite_id, date, is_available);
