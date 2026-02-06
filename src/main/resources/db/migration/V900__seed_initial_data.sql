-- Seed initial data for Food Shop application
-- Categories and sample food items

-- Insert Categories
INSERT INTO categories (name, description, display_order, active, image_url, created_at)
VALUES 
    ('Pizza', 'Delicious wood-fired pizzas with fresh ingredients', 1, true, NULL, CURRENT_TIMESTAMP),
    ('Burgers', 'Juicy burgers made with premium beef', 2, true, NULL, CURRENT_TIMESTAMP),
    ('Salads', 'Fresh and healthy salad options', 3, true, NULL, CURRENT_TIMESTAMP),
    ('Desserts', 'Sweet treats to complete your meal', 4, true, NULL, CURRENT_TIMESTAMP),
    ('Beverages', 'Refreshing drinks and smoothies', 5, true, NULL, CURRENT_TIMESTAMP);

-- Insert Food Items (Pizza Category)
INSERT INTO food_items (category_id, name, description, price, available, image_url, thumbnail_url, avg_rating, rating_count, created_at)
VALUES 
    ((SELECT id FROM categories WHERE name = 'Pizza'), 'Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and fresh basil', 12.99, true, NULL, NULL, 4.5, 127, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Pizza'), 'Pepperoni Pizza', 'Traditional pepperoni pizza with extra cheese', 14.99, true, NULL, NULL, 4.7, 215, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Pizza'), 'Vegetarian Supreme', 'Loaded with bell peppers, onions, mushrooms, and olives', 13.99, true, NULL, NULL, 4.3, 98, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Pizza'), 'BBQ Chicken Pizza', 'Grilled chicken with BBQ sauce and red onions', 15.99, true, NULL, NULL, 4.6, 156, CURRENT_TIMESTAMP);

-- Insert Food Items (Burgers Category)
INSERT INTO food_items (category_id, name, description, price, available, image_url, thumbnail_url, avg_rating, rating_count, created_at)
VALUES 
    ((SELECT id FROM categories WHERE name = 'Burgers'), 'Classic Cheeseburger', 'Quarter pound beef patty with cheese, lettuce, tomato, and pickles', 9.99, true, NULL, NULL, 4.4, 189, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Burgers'), 'Bacon Deluxe Burger', 'Premium burger with crispy bacon and special sauce', 11.99, true, NULL, NULL, 4.8, 267, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Burgers'), 'Veggie Burger', 'Plant-based patty with avocado and chipotle mayo', 10.99, true, NULL, NULL, 4.2, 76, CURRENT_TIMESTAMP);

-- Insert Food Items (Salads Category)
INSERT INTO food_items (category_id, name, description, price, available, image_url, thumbnail_url, avg_rating, rating_count, created_at)
VALUES 
    ((SELECT id FROM categories WHERE name = 'Salads'), 'Caesar Salad', 'Crisp romaine lettuce with Caesar dressing and croutons', 8.99, true, NULL, NULL, 4.1, 92, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Salads'), 'Greek Salad', 'Fresh vegetables with feta cheese and olives', 9.99, true, NULL, NULL, 4.5, 134, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Salads'), 'Grilled Chicken Salad', 'Mixed greens with grilled chicken breast', 11.99, true, NULL, NULL, 4.6, 145, CURRENT_TIMESTAMP);

-- Insert Food Items (Desserts Category)
INSERT INTO food_items (category_id, name, description, price, available, image_url, thumbnail_url, avg_rating, rating_count, created_at)
VALUES 
    ((SELECT id FROM categories WHERE name = 'Desserts'), 'Chocolate Brownie', 'Rich chocolate brownie with vanilla ice cream', 6.99, true, NULL, NULL, 4.7, 201, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Desserts'), 'New York Cheesecake', 'Creamy cheesecake with berry compote', 7.99, true, NULL, NULL, 4.8, 178, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Desserts'), 'Tiramisu', 'Classic Italian dessert with espresso and mascarpone', 7.99, true, NULL, NULL, 4.6, 123, CURRENT_TIMESTAMP);

-- Insert Food Items (Beverages Category)
INSERT INTO food_items (category_id, name, description, price, available, image_url, thumbnail_url, avg_rating, rating_count, created_at)
VALUES 
    ((SELECT id FROM categories WHERE name = 'Beverages'), 'Fresh Orange Juice', 'Freshly squeezed orange juice', 4.99, true, NULL, NULL, 4.3, 87, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Beverages'), 'Berry Smoothie', 'Mixed berry smoothie with yogurt', 5.99, true, NULL, NULL, 4.5, 112, CURRENT_TIMESTAMP),
    ((SELECT id FROM categories WHERE name = 'Beverages'), 'Iced Coffee', 'Cold brew coffee with ice', 3.99, true, NULL, NULL, 4.4, 156, CURRENT_TIMESTAMP);
