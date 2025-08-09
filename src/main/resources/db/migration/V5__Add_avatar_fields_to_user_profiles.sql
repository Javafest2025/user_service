-- Add avatar fields to user_profiles table
ALTER TABLE user_profiles
ADD COLUMN avatar_key TEXT,
ADD COLUMN avatar_etag TEXT,
ADD COLUMN avatar_updated_at TIMESTAMP;

-- Create index for avatar_key for better query performance
CREATE INDEX idx_user_profiles_avatar_key ON user_profiles(avatar_key);

-- Add comments to new columns (only for PostgreSQL)
-- #!postgresql
COMMENT ON COLUMN user_profiles.avatar_key IS 'Backblaze B2 object key for the avatar image';
COMMENT ON COLUMN user_profiles.avatar_etag IS 'ETag of the uploaded avatar image for validation';
COMMENT ON COLUMN user_profiles.avatar_updated_at IS 'Timestamp when avatar was last updated';
