-- Migration: Create categories table
-- Description: Stores food item categories for catalog organization

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(512),
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for ordered listing
CREATE INDEX idx_category_display_order ON categories(display_order) WHERE active = TRUE;
CREATE INDEX idx_category_active ON categories(active);

-- Comments
COMMENT ON TABLE categories IS 'Food item categories for catalog organization';
COMMENT ON COLUMN categories.display_order IS 'Order for displaying categories (lower numbers first)';
COMMENT ON COLUMN categories.active IS 'Whether category is visible to customers';
