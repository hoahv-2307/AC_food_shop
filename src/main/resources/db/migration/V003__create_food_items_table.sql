-- Migration: Create food_items table
-- Description: Stores food products available for purchase

CREATE TABLE food_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    image_url VARCHAR(512),
    thumbnail_url VARCHAR(512),
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    avg_rating DECIMAL(3, 2) DEFAULT 0.00 CHECK (avg_rating >= 0 AND avg_rating <= 5),
    rating_count INT DEFAULT 0 CHECK (rating_count >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_food_item_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Indexes for performance
CREATE INDEX idx_food_items_category ON food_items(category_id);
CREATE INDEX idx_food_items_available ON food_items(available);
CREATE INDEX idx_food_items_avg_rating ON food_items(avg_rating DESC) WHERE available = TRUE;
CREATE INDEX idx_food_items_name_search ON food_items USING gin(to_tsvector('english', name || ' ' || description));

-- Comments
COMMENT ON TABLE food_items IS 'Food products available for purchase';
COMMENT ON COLUMN food_items.avg_rating IS 'Cached average rating (0-5 stars) calculated from ratings table';
COMMENT ON COLUMN food_items.rating_count IS 'Cached count of ratings for this item';
COMMENT ON COLUMN food_items.available IS 'Whether item is available for order';
