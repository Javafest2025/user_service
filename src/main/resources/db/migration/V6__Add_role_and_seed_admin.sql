-- Migration to seed admin user
-- Note: Role column already exists from V1, so we just need to seed an admin

-- Seed exactly one ADMIN user (idempotent on email)
-- bcrypt('password') = $2a$10$CwTycUXWue0Thq9StjUM0uJ8S.fPTsCkXbkqZgf/mBlC9ZwTe74yK
INSERT INTO users (id, email, encrypted_password, role, is_email_confirmed, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'arisu.emanator27@gmail.com',
  '$2a$10$CwTycUXWue0Thq9StjUM0uJ8S.fPTsCkXbkqZgf/mBlC9ZwTe74yK', -- bcrypt('password')
  'ADMIN',
  true,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO UPDATE SET 
  role = 'ADMIN',
  updated_at = CURRENT_TIMESTAMP;