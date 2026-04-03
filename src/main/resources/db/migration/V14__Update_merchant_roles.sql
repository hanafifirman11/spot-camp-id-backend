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
    '$2a$12$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', -- password: password
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
