-- Migration: Create ratings table
-- Description: Customer ratings and reviews for food items

CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    food_item_id BIGINT NOT NULL REFERENCES food_items(id) ON DELETE CASCADE,
    stars INT NOT NULL CHECK (stars >= 1 AND stars <= 5),
    review_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_rating_food FOREIGN KEY (food_item_id) REFERENCES food_items(id),
    CONSTRAINT uk_user_food_item_rating UNIQUE (user_id, food_item_id)
);

-- Indexes for performance
CREATE INDEX idx_ratings_food_item ON ratings(food_item_id);
CREATE INDEX idx_ratings_user ON ratings(user_id);
CREATE INDEX idx_ratings_verified ON ratings(verified_purchase) WHERE verified_purchase = TRUE;
CREATE INDEX idx_ratings_created_at ON ratings(created_at DESC);

-- Comments
COMMENT ON TABLE ratings IS 'Customer ratings and reviews';
COMMENT ON COLUMN ratings.stars IS 'Rating from 1 to 5 stars';
COMMENT ON COLUMN ratings.verified_purchase IS 'Whether user has actually ordered this item';
COMMENT ON CONSTRAINT uk_user_food_item_rating ON ratings IS 'Each user can only rate a food item once';
