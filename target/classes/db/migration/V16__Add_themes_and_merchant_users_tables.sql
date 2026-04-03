-- V16: Add Themes and Merchant Users Tables
-- Purpose: Add themes table and modify users table for merchant user management
-- Database: PostgreSQL

-- ==================== Themes ====================
CREATE TABLE IF NOT EXISTS themes (
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

-- ==================== Users ====================
-- Remove unique constraint on business_code to allow multiple users per merchant account
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_business_code_key;
-- Also try dropping index if it was a unique index not a constraint (common in some migration tools)
DROP INDEX IF EXISTS users_business_code_key;

-- Re-create as non-unique index for performance
CREATE INDEX IF NOT EXISTS idx_users_business_code ON users (business_code);

-- ==================== Triggers ====================
CREATE TRIGGER update_themes_updated_at
    BEFORE UPDATE ON themes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
