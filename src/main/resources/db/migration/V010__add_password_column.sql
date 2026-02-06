-- Add password column to users table for local authentication
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255);

-- Update provider enum to include LOCAL
-- Note: PostgreSQL doesn't require explicit enum update if using VARCHAR for enum storage
