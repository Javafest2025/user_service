-- Create users table
CREATE TABLE users (
    id ${uuid_type} PRIMARY KEY ${uuid_default},
    email VARCHAR(255) NOT NULL UNIQUE,
    encrypted_password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_email_confirmed BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for faster lookups
CREATE INDEX idx_users_email ON users(email);

-- Create index on role for role-based queries
CREATE INDEX idx_users_role ON users(role);

-- Create index on last_login_at for analytics
CREATE INDEX idx_users_last_login ON users(last_login_at);

-- Add comment to table (only for PostgreSQL)
-- #!postgresql
COMMENT ON TABLE users IS 'Core user accounts for ScholarAI platform';
COMMENT ON COLUMN users.id IS 'Unique identifier for the user';
COMMENT ON COLUMN users.email IS 'User email address, must be unique';
COMMENT ON COLUMN users.encrypted_password IS 'Hashed password using bcrypt or similar';
COMMENT ON COLUMN users.role IS 'User role: STUDENT, PROFESSOR, ADMIN, etc.';
COMMENT ON COLUMN users.is_email_confirmed IS 'Whether user has confirmed their email address';
COMMENT ON COLUMN users.last_login_at IS 'Timestamp of last successful login';
COMMENT ON COLUMN users.created_at IS 'Timestamp when user account was created';
COMMENT ON COLUMN users.updated_at IS 'Timestamp when user account was last updated'; 