ALTER TABLE users ADD COLUMN IF NOT EXISTS business_code VARCHAR(20);
ALTER TABLE campsites ADD COLUMN IF NOT EXISTS code VARCHAR(20);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_business_code ON users (business_code);
CREATE UNIQUE INDEX IF NOT EXISTS idx_campsites_code ON campsites (code);
