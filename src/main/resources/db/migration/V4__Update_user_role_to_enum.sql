-- Migration to update user role column to use enum values
-- First, update existing data to use proper enum values
UPDATE users SET role = 'USER' WHERE role NOT IN ('USER', 'ADMIN') OR role IS NULL;

-- Add a check constraint to ensure only valid enum values
ALTER TABLE users ADD CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN'));

-- Note: The column type remains VARCHAR since we're using @Enumerated(EnumType.STRING)
-- This allows for easy reading and debugging while maintaining type safety in Java 