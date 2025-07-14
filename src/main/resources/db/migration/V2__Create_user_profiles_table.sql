-- Create user_profiles table
CREATE TABLE user_profiles (
    id ${uuid_type} PRIMARY KEY ${uuid_default},
    user_id ${uuid_type} NOT NULL UNIQUE,
    full_name VARCHAR(255),
    avatar_url TEXT,
    phone_number VARCHAR(20),
    date_of_birth TIMESTAMP,
    bio TEXT,
    affiliation VARCHAR(255),
    position_title VARCHAR(255),
    research_interests TEXT,
    google_scholar_url TEXT,
    personal_website_url TEXT,
    orcid_id VARCHAR(50),
    linkedin_url TEXT,
    twitter_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_full_name ON user_profiles(full_name);
CREATE INDEX idx_user_profiles_affiliation ON user_profiles(affiliation);
CREATE INDEX idx_user_profiles_orcid_id ON user_profiles(orcid_id);

-- Add comments to table and columns (only for PostgreSQL)
-- #!postgresql
COMMENT ON TABLE user_profiles IS 'Extended user profile information for ScholarAI platform';
COMMENT ON COLUMN user_profiles.id IS 'Unique identifier for the user profile';
COMMENT ON COLUMN user_profiles.user_id IS 'Foreign key reference to users table';
COMMENT ON COLUMN user_profiles.full_name IS 'User full name (first and last name)';
COMMENT ON COLUMN user_profiles.avatar_url IS 'URL to user profile picture';
COMMENT ON COLUMN user_profiles.phone_number IS 'User phone number';
COMMENT ON COLUMN user_profiles.date_of_birth IS 'User date of birth';
COMMENT ON COLUMN user_profiles.bio IS 'User biography or description';
COMMENT ON COLUMN user_profiles.affiliation IS 'User institutional affiliation (university, company, etc.)';
COMMENT ON COLUMN user_profiles.position_title IS 'User job title or academic position';
COMMENT ON COLUMN user_profiles.research_interests IS 'User research interests or areas of expertise';
COMMENT ON COLUMN user_profiles.google_scholar_url IS 'URL to Google Scholar profile';
COMMENT ON COLUMN user_profiles.personal_website_url IS 'URL to personal website';
COMMENT ON COLUMN user_profiles.orcid_id IS 'ORCID identifier for academic users';
COMMENT ON COLUMN user_profiles.linkedin_url IS 'URL to LinkedIn profile';
COMMENT ON COLUMN user_profiles.twitter_url IS 'URL to Twitter/X profile';
COMMENT ON COLUMN user_profiles.created_at IS 'Timestamp when profile was created';
COMMENT ON COLUMN user_profiles.updated_at IS 'Timestamp when profile was last updated'; 