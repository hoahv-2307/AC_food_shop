-- Add admin user for login
-- Password is: Admin@123 (BCrypt encoded)

INSERT INTO users (email, name, avatar_url, provider, external_id, role, status, created_at, password)
VALUES (
    'admin@foodshop.com',
    'Admin User',
    NULL,
    'LOCAL',
    'admin-local',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    '$2a$10$8qKv8RXUqR2kHtjKYKy0m.7uZkCZL8v5J1W5VfLQT6YBgZM/JYp8e'
);
