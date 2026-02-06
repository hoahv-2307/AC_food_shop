-- Migration: Create orders and order_items tables
-- Description: Customer orders with payment tracking

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'DELIVERED', 'CANCELLED')),
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    stripe_session_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    food_item_id BIGINT NOT NULL REFERENCES food_items(id) ON DELETE RESTRICT,
    quantity INT NOT NULL CHECK (quantity > 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_item_food FOREIGN KEY (food_item_id) REFERENCES food_items(id)
);

-- Indexes for performance
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_stripe_session ON orders(stripe_session_id) WHERE stripe_session_id IS NOT NULL;
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_food ON order_items(food_item_id);

-- Comments
COMMENT ON TABLE orders IS 'Customer orders';
COMMENT ON COLUMN orders.status IS 'Order status workflow: PENDING → CONFIRMED → PREPARING → DELIVERED';
COMMENT ON COLUMN orders.stripe_session_id IS 'Stripe Checkout Session ID for payment tracking';
COMMENT ON COLUMN order_items.price IS 'Price at time of order (frozen, not affected by future price changes)';
