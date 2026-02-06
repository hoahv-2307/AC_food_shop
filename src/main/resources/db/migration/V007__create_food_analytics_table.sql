-- V007: Create food_analytics table for tracking view and order counts
-- Feature: Food Analytics Dashboard and Monthly Reporting
-- Date: 2026-02-06

CREATE TABLE food_analytics (
    id BIGSERIAL PRIMARY KEY,
    food_item_id BIGINT NOT NULL UNIQUE,
    view_count BIGINT NOT NULL DEFAULT 0,
    order_count BIGINT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_food_analytics_food_item 
        FOREIGN KEY (food_item_id) 
        REFERENCES food_items(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_food_analytics_food_item ON food_analytics(food_item_id);
CREATE INDEX idx_food_analytics_view_count ON food_analytics(view_count DESC);
CREATE INDEX idx_food_analytics_order_count ON food_analytics(order_count DESC);

-- Initialize analytics for all existing food items
INSERT INTO food_analytics (food_item_id, view_count, order_count)
SELECT id, 0, 0 FROM food_items
ON CONFLICT (food_item_id) DO NOTHING;

COMMENT ON TABLE food_analytics IS 'Tracks cumulative view and order counts for food items';
COMMENT ON COLUMN food_analytics.version IS 'Optimistic locking version for concurrent updates';
COMMENT ON COLUMN food_analytics.view_count IS 'Cumulative count of food detail page views (session-deduplicated)';
COMMENT ON COLUMN food_analytics.order_count IS 'Cumulative count of completed orders containing this food item';
