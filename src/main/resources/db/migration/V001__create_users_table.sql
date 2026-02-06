-- Migration: Create users table
-- Description: Stores user accounts with OAuth2 social authentication support

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(512),
    provider VARCHAR(20) NOT NULL CHECK (provider IN ('GOOGLE', 'FACEBOOK', 'LOCAL')),
    external_id VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'ADMIN')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DEACTIVATED', 'DELETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT uk_provider_external_id UNIQUE (provider, external_id)
);

-- Indexes for performance
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_provider_external ON users(provider, external_id);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_status ON users(status);

-- Comments
COMMENT ON TABLE users IS 'User accounts with OAuth2 authentication';
COMMENT ON COLUMN users.provider IS 'OAuth2 provider: GOOGLE or FACEBOOK';
COMMENT ON COLUMN users.external_id IS 'User ID from OAuth2 provider';
COMMENT ON COLUMN users.role IS 'User role: CUSTOMER or ADMIN';
COMMENT ON COLUMN users.status IS 'Account status: ACTIVE, DEACTIVATED, or DELETED (soft delete)';
