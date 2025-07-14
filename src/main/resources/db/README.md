# Database Schema Documentation

## Overview
This directory contains Flyway database migrations for the ScholarAI User Service. The migrations create and manage the database schema for user authentication, profiles, and identity providers.

## Migration Files

### V1__Create_users_table.sql
Creates the core `users` table with the following structure:
- `id`: UUID primary key
- `email`: Unique email address
- `encrypted_password`: Hashed password
- `role`: User role (STUDENT, PROFESSOR, ADMIN, etc.)
- `is_email_confirmed`: Email verification status
- `last_login_at`: Last login timestamp
- `created_at` / `updated_at`: Audit timestamps

### V2__Create_user_profiles_table.sql
Creates the `user_profiles` table for extended user information:
- `id`: UUID primary key
- `user_id`: Foreign key to users table (one-to-one relationship)
- `full_name`: User's full name
- `avatar_url`: Profile picture URL
- `phone_number`: Contact phone number
- `date_of_birth`: Birth date
- `bio`: User biography
- `affiliation`: Institutional affiliation
- `position_title`: Job title or academic position
- `research_interests`: Research areas
- `google_scholar_url`: Google Scholar profile
- `personal_website_url`: Personal website
- `orcid_id`: ORCID identifier
- `linkedin_url`: LinkedIn profile
- `twitter_url`: Twitter/X profile
- `created_at` / `updated_at`: Audit timestamps

### V3__Create_user_identity_providers_table.sql
Creates the `user_identity_providers` table for OAuth/social login:
- `id`: UUID primary key
- `user_id`: Foreign key to users table (many-to-one relationship)
- `provider`: Identity provider name (GOOGLE, FACEBOOK, GITHUB, etc.)
- `provider_user_id`: User ID from external provider
- `created_at` / `updated_at`: Audit timestamps

## Indexes and Constraints

### Users Table
- Unique index on `email`
- Index on `role` for role-based queries
- Index on `last_login_at` for analytics

### User Profiles Table
- Foreign key constraint to users table with CASCADE delete
- Indexes on `user_id`, `full_name`, `affiliation`, and `orcid_id`

### User Identity Providers Table
- Foreign key constraint to users table with CASCADE delete
- Unique composite index on `(user_id, provider, provider_user_id)`
- Indexes on `user_id`, `provider`, and `provider_user_id`

## Running Migrations

### Local Development
```bash
# Start the application - Flyway will run migrations automatically
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### Production
```bash
# Ensure database is accessible and environment variables are set
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

### Manual Migration
```bash
# Run Flyway migrations manually
./mvnw flyway:migrate
```

## Environment Variables

### Local Development
- `USER_DB_PORT`: PostgreSQL port (default: 5432)
- `USER_DB_USER`: Database username
- `USER_DB_PASSWORD`: Database password

### Production
- `DB_HOST`: Database host
- `DB_PORT`: Database port
- `DB_NAME`: Database name
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

## Database Schema Diagram

```
users (1) ←→ (1) user_profiles
  ↓
  (1) ←→ (N) user_identity_providers
```

## Notes
- All tables use UUID primary keys for better distribution and security
- Timestamps are automatically managed by Hibernate
- Foreign key constraints ensure referential integrity
- Indexes are optimized for common query patterns
- The schema supports both traditional email/password and OAuth authentication

## Flyway Migration Guidelines

### Creating New Migrations

When you need to add new tables, modify existing tables, or add data migrations:

1. **Create a new migration file** in `src/main/resources/db/migration/`
2. **Follow the naming convention**: `V{version}__{description}.sql`
   - Example: `V4__Add_user_preferences_table.sql`
   - Example: `V5__Add_index_to_users_email.sql`
   - Example: `V6__Insert_default_roles.sql`

3. **Version numbering rules**:
   - Use sequential numbers (V1, V2, V3, V4, etc.)
   - Never reuse a version number
   - Never modify existing migration files once they've been applied to production

### Migration File Structure

```sql
-- V4__Add_user_preferences_table.sql
-- Migration description: Add user preferences table for storing user settings

-- Create the new table
CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    theme VARCHAR(20) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'en',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_preferences_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add indexes for performance
CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);

-- Add comments
COMMENT ON TABLE user_preferences IS 'User preferences and settings';
COMMENT ON COLUMN user_preferences.theme IS 'User interface theme preference';
```

### Common Migration Patterns

#### Adding a New Table
```sql
-- V4__Create_new_table.sql
CREATE TABLE new_table (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- other columns
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes
CREATE INDEX idx_new_table_column ON new_table(column_name);

-- Add comments
COMMENT ON TABLE new_table IS 'Description of the table';
```

#### Modifying Existing Tables
```sql
-- V5__Add_column_to_users.sql
ALTER TABLE users ADD COLUMN new_column VARCHAR(255);

-- Add index if needed
CREATE INDEX idx_users_new_column ON users(new_column);
```

#### Adding Data Migrations
```sql
-- V6__Insert_default_roles.sql
INSERT INTO users (id, email, encrypted_password, role, is_email_confirmed) 
VALUES 
    (gen_random_uuid(), 'admin@scholarai.dev', '$2a$10$...', 'ADMIN', true),
    (gen_random_uuid(), 'support@scholarai.dev', '$2a$10$...', 'SUPPORT', true);
```

#### Renaming Columns/Tables
```sql
-- V7__Rename_column.sql
ALTER TABLE users RENAME COLUMN old_column_name TO new_column_name;
```

### Flyway Commands

#### Check Migration Status
```bash
# View current migration status
./mvnw flyway:info

# Check which migrations are pending
./mvnw flyway:info -Dflyway.locations=classpath:db/migration
```

#### Run Migrations
```bash
# Run all pending migrations
./mvnw flyway:migrate

# Run migrations with specific profile
./mvnw flyway:migrate -Dspring.profiles.active=local
```

#### Validate Migrations
```bash
# Validate migration files without running them
./mvnw flyway:validate
```

#### Clean Database (Development Only)
```bash
# ⚠️ WARNING: This will drop all tables and data!
# Only use in development environments
./mvnw flyway:clean
```

#### Repair Migrations
```bash
# Fix migration state if there are issues
./mvnw flyway:repair
```

### Best Practices

#### 1. **Never Modify Applied Migrations**
- Once a migration has been applied to production, never modify it
- Create a new migration to make changes instead

#### 2. **Test Migrations Locally First**
```bash
# Test migrations on local database
./mvnw flyway:migrate -Dspring.profiles.active=local
```

#### 3. **Use Descriptive Names**
- Migration names should clearly describe what they do
- Include the table/feature name in the description

#### 4. **Include Comments**
- Add comments to explain complex migrations
- Document the purpose of indexes and constraints

#### 5. **Handle Rollbacks Carefully**
- Flyway doesn't support automatic rollbacks
- Plan your migrations to be forward-only
- Test migrations thoroughly before production

#### 6. **Version Control**
- Always commit migration files to version control
- Include migration files in your deployment process

### Troubleshooting

#### Migration Fails
```bash
# Check migration status
./mvnw flyway:info

# Repair if needed
./mvnw flyway:repair

# Check logs for specific errors
./mvnw spring-boot:run -Dspring.profiles.active=local
```

#### Database Connection Issues
- Verify database is running and accessible
- Check environment variables for database credentials
- Ensure database user has proper permissions

#### Migration Order Issues
- Flyway runs migrations in version order
- Ensure version numbers are sequential
- Use `flyway:info` to check current state

### Environment-Specific Considerations

#### Development
- Use `flyway:clean` to reset database during development
- Enable `ddl-auto: validate` to catch schema mismatches
- Use detailed logging for debugging

#### Production
- Never use `flyway:clean` in production
- Always test migrations on staging first
- Use `ddl-auto: validate` to ensure schema consistency
- Monitor migration execution in logs

### Migration Checklist

Before creating a new migration:

- [ ] Database is accessible and running
- [ ] Previous migrations are applied successfully
- [ ] Migration file follows naming convention
- [ ] Migration includes necessary indexes
- [ ] Migration includes appropriate comments
- [ ] Migration is tested locally
- [ ] Migration handles edge cases (null values, constraints)
- [ ] Migration is committed to version control 