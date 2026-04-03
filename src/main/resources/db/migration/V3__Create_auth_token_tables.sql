-- V3: Authentication Token Tables for Spot Camp ID Backend
-- Purpose: Manage JWT refresh tokens, email verification, and password reset
-- Database: PostgreSQL

-- ==================== Refresh Tokens ====================
-- Stores refresh tokens for JWT-based authentication
-- Allows token revocation and device tracking
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,  -- SHA-256 hash of token
    device_info VARCHAR(500),  -- User agent / device description
    ip_address VARCHAR(45),    -- IPv4 or IPv6
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_active ON refresh_tokens(user_id, revoked, expires_at);

-- ==================== Email Verification Tokens ====================
-- Stores tokens for email verification flow
CREATE TABLE email_verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,  -- Email being verified
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verification_token ON email_verification_tokens(token);
CREATE INDEX idx_email_verification_user ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_expires ON email_verification_tokens(expires_at);

-- ==================== Password Reset Tokens ====================
-- Stores tokens for password reset flow
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,  -- SHA-256 hash of token
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    ip_address VARCHAR(45),  -- IP that requested reset
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token ON password_reset_tokens(token_hash);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at);

-- ==================== User Sessions (Optional - for session tracking) ====================
-- Track active user sessions for security auditing
CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    location VARCHAR(200),  -- Geo-location if available
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logged_out_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_user ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_session ON user_sessions(session_id);
CREATE INDEX idx_user_sessions_active ON user_sessions(user_id, is_active);
CREATE INDEX idx_user_sessions_last_activity ON user_sessions(last_activity_at);

-- ==================== Failed Login Attempts ====================
-- Track failed login attempts for security (rate limiting, lockout)
CREATE TABLE failed_login_attempts (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    attempt_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    failure_reason VARCHAR(50) NOT NULL,  -- INVALID_PASSWORD, USER_NOT_FOUND, ACCOUNT_LOCKED, etc.

    CONSTRAINT chk_failure_reason CHECK (failure_reason IN ('INVALID_PASSWORD', 'USER_NOT_FOUND', 'ACCOUNT_LOCKED', 'ACCOUNT_SUSPENDED', 'EMAIL_NOT_VERIFIED'))
);

CREATE INDEX idx_failed_login_email ON failed_login_attempts(email);
CREATE INDEX idx_failed_login_ip ON failed_login_attempts(ip_address);
CREATE INDEX idx_failed_login_time ON failed_login_attempts(attempt_time);
CREATE INDEX idx_failed_login_email_time ON failed_login_attempts(email, attempt_time);
CREATE INDEX idx_failed_login_ip_time ON failed_login_attempts(ip_address, attempt_time);

-- ==================== Cleanup Functions ====================

-- Function to cleanup expired tokens (run periodically via scheduler)
CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS void AS $$
BEGIN
    -- Delete expired refresh tokens
    DELETE FROM refresh_tokens WHERE expires_at < NOW() - INTERVAL '7 days';

    -- Delete expired and unused email verification tokens
    DELETE FROM email_verification_tokens WHERE expires_at < NOW() - INTERVAL '1 day';

    -- Delete expired and unused password reset tokens
    DELETE FROM password_reset_tokens WHERE expires_at < NOW() - INTERVAL '1 day';

    -- Delete old failed login attempts (keep last 30 days)
    DELETE FROM failed_login_attempts WHERE attempt_time < NOW() - INTERVAL '30 days';

    -- Delete inactive sessions older than 30 days
    DELETE FROM user_sessions WHERE is_active = FALSE AND logged_out_at < NOW() - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

-- Function to check if user is locked out due to failed attempts
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

-- Function to revoke all refresh tokens for a user (logout from all devices)
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
