-- Initial Schema for Spot Camp ID Backend
-- Database: PostgreSQL
-- Version: 1.0

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
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_role CHECK (role IN ('CAMPER', 'MERCHANT', 'ADMIN')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT chk_business_name CHECK (
        (role = 'MERCHANT' AND business_name IS NOT NULL) OR 
        (role != 'MERCHANT')
    )
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

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
    CONSTRAINT chk_product_type CHECK (type IN ('RENTAL', 'SALE')),
    CONSTRAINT chk_product_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_base_price CHECK (base_price >= 0),
    
    -- Rental constraints
    CONSTRAINT chk_rental_fields CHECK (
        (type = 'RENTAL' AND stock_total IS NOT NULL AND daily_rate IS NOT NULL) OR
        (type != 'RENTAL' AND stock_total IS NULL AND daily_rate IS NULL)
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
-- Partial index not fully supported in H2 with this syntax, handled by app logic
-- CREATE UNIQUE INDEX idx_map_config_active ON map_configurations(campsite_id) 
--    WHERE status = 'ACTIVE';

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
    CONSTRAINT chk_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_total_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_contact_info CHECK (
        (status IN ('PAYMENT_PENDING', 'CONFIRMED', 'COMPLETED') AND 
         contact_name IS NOT NULL AND contact_email IS NOT NULL) OR
        (status IN ('IN_CART', 'CANCELLED'))
    )
);

CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_campsite ON bookings(campsite_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_bookings_spot ON bookings(campsite_id, spot_id);
-- CREATE UNIQUE INDEX idx_bookings_invoice ON bookings(invoice_number) WHERE invoice_number IS NOT NULL;

CREATE TABLE booking_items (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(12, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    
    CONSTRAINT fk_booking_items_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_item_subtotal CHECK (subtotal >= 0)
);

-- ==================== Inventory Locks ====================
CREATE TABLE inventory_locks (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    spot_id VARCHAR(100),
    quantity INTEGER NOT NULL,
    lock_type VARCHAR(20) NOT NULL,
    start_date DATE,
    end_date DATE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_inventory_locks_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inventory_locks_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT chk_lock_type CHECK (lock_type IN ('CART', 'CONFIRMED')),
    CONSTRAINT chk_lock_quantity CHECK (quantity > 0),
    CONSTRAINT chk_lock_dates CHECK (
        (lock_type = 'CART' AND start_date IS NOT NULL AND end_date IS NOT NULL AND end_date > start_date) OR
        (lock_type != 'CART')
    )
);

CREATE INDEX idx_inventory_locks_product ON inventory_locks(product_id);
CREATE INDEX idx_inventory_locks_booking ON inventory_locks(booking_id);
CREATE INDEX idx_inventory_locks_spot ON inventory_locks(spot_id);
CREATE INDEX idx_inventory_locks_dates ON inventory_locks(start_date, end_date);
CREATE INDEX idx_inventory_locks_expires ON inventory_locks(expires_at);

-- ==================== Reviews ====================
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    campsite_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    title VARCHAR(200),
    review_text TEXT,
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

-- ==================== Insert Default Data ====================

-- Insert default amenities
INSERT INTO amenities (name, icon, category) VALUES
('WiFi', 'wifi', 'connectivity'),
('Parking', 'parking', 'facilities'),
('Shower', 'shower', 'facilities'),
('Toilet', 'toilet', 'facilities'),
('Kitchen', 'kitchen', 'facilities'),
('Fire Pit', 'fire', 'outdoor'),
('Picnic Table', 'table', 'outdoor'),
('Electricity', 'power', 'utilities'),
('Water Access', 'water', 'utilities'),
('Pet Friendly', 'pet', 'policy'),
('Swimming Pool', 'pool', 'recreation'),
('Playground', 'playground', 'recreation'),
('Hiking Trails', 'hiking', 'outdoor'),
('Lake View', 'lake', 'view'),
('Mountain View', 'mountain', 'view'),
('Security', 'security', 'safety'),
('First Aid', 'medical', 'safety'),
('24/7 Access', 'clock', 'convenience'),
('Laundry', 'laundry', 'facilities'),
('Restaurant', 'restaurant', 'dining');
