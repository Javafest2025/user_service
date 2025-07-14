-- Create user_identity_providers table
CREATE TABLE user_identity_providers (
    id ${uuid_type} PRIMARY KEY ${uuid_default},
    user_id ${uuid_type} NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_identity_providers_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create unique constraint to prevent duplicate provider-user combinations
CREATE UNIQUE INDEX idx_user_identity_providers_unique ON user_identity_providers(user_id, provider, provider_user_id);

-- Create indexes for better query performance
CREATE INDEX idx_user_identity_providers_user_id ON user_identity_providers(user_id);
CREATE INDEX idx_user_identity_providers_provider ON user_identity_providers(provider);
CREATE INDEX idx_user_identity_providers_provider_user_id ON user_identity_providers(provider_user_id);

-- Add comments to table and columns (only for PostgreSQL)
-- #!postgresql
COMMENT ON TABLE user_identity_providers IS 'OAuth and social login provider mappings for users';
COMMENT ON COLUMN user_identity_providers.id IS 'Unique identifier for the identity provider mapping';
COMMENT ON COLUMN user_identity_providers.user_id IS 'Foreign key reference to users table';
COMMENT ON COLUMN user_identity_providers.provider IS 'Identity provider name (GOOGLE, FACEBOOK, GITHUB, etc.)';
COMMENT ON COLUMN user_identity_providers.provider_user_id IS 'User ID from the external provider';
COMMENT ON COLUMN user_identity_providers.created_at IS 'Timestamp when mapping was created';
COMMENT ON COLUMN user_identity_providers.updated_at IS 'Timestamp when mapping was last updated'; 