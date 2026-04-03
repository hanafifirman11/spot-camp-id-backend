-- Development seed data (idempotent)

-- Update existing MERCHANT role to MERCHANT_ADMIN
UPDATE users
SET role = 'MERCHANT_ADMIN'
WHERE role = 'MERCHANT';

-- Update merchant@spotcamp.id to have business_code if exists
UPDATE users
SET business_code = 'SPOTCAMP001'
WHERE email = 'merchant@spotcamp.id';

-- Create MERCHANT_MEMBER user for testing (only if not exists)
INSERT INTO users (email, password_hash, first_name, last_name, role, business_name, business_code, email_verified, status, created_at, updated_at)
SELECT
    'member@spotcamp.id',
    '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u',
    'Jane',
    'Doe',
    'MERCHANT_MEMBER',
    'SpotCamp Official',
    'SPOTCAMP001',
    TRUE,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'member@spotcamp.id'
);

-- Insert sample users
INSERT INTO users (email, password_hash, first_name, last_name, phone, role, business_name, business_code, email_verified, status, created_at, updated_at, created_by, updated_by)
SELECT 'merchant@spotcamp.id', '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 'Admin', 'SpotCamp', NULL, 'MERCHANT_ADMIN', 'SpotCamp Official', 'SPOTCAMP001', TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'merchant@spotcamp.id');

INSERT INTO users (email, password_hash, first_name, last_name, phone, role, business_name, business_code, email_verified, status, created_at, updated_at, created_by, updated_by)
SELECT 'camper1@spotcamp.id', '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 'Hanafi', 'Firman', NULL, 'CAMPER', NULL, NULL, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'camper1@spotcamp.id');

INSERT INTO users (email, password_hash, first_name, last_name, phone, role, business_name, business_code, email_verified, status, created_at, updated_at, created_by, updated_by)
SELECT 'camper2@spotcamp.id', '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 'Ayu', 'Putri', NULL, 'CAMPER', NULL, NULL, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'camper2@spotcamp.id');

INSERT INTO users (email, password_hash, first_name, last_name, phone, role, business_name, business_code, email_verified, status, created_at, updated_at, created_by, updated_by)
SELECT 'superadmin@spotcamp.id', '$2a$12$JE9E4tFq5.3YABFlxL57sO4rK1z6wNpqoeNHApE/aiGoty6TvgHdy', 'Super', 'Admin', NULL, 'SUPERADMIN', 'SpotCamp HQ', NULL, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'superadmin@spotcamp.id');
