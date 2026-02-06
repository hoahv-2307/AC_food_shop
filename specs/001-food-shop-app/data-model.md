# Data Model: Food Shop Application

**Feature**: Food Shop Application  
**Date**: 2026-02-06  
**Phase**: 1 - Data Model Design

## Overview

This document defines the database schema for the Food Shop Application using JPA/Hibernate entities mapped to PostgreSQL tables. The model supports all 6 user stories: browsing/ordering, ratings/sharing, and admin management functionality.

## Entity Relationship Diagram

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│    User     │1       *│    Order     │1       *│  OrderItem   │
│─────────────│◄────────│──────────────│◄────────│──────────────│
│ id (PK)     │         │ id (PK)      │         │ id (PK)      │
│ email       │         │ user_id (FK) │         │ order_id(FK) │
│ name        │         │ status       │         │ food_item_id │
│ avatar_url  │         │ total_amount │         │ quantity     │
│ provider    │         │ created_at   │         │ price        │
│ external_id │         │ updated_at   │         └──────────────┘
│ role        │         └──────────────┘                │
│ status      │                                         │
│ created_at  │                                         │
└─────────────┘                                         │
      │                                                 │
      │1                                                │
      │                                                 │
      │*                                                │*
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│   Rating    │*       1│  FoodItem    │1       *│  Category    │
│─────────────│────────►│──────────────│◄────────│──────────────│
│ id (PK)     │         │ id (PK)      │         │ id (PK)      │
│ user_id(FK) │         │ name         │         │ name         │
│ food_item_id│         │ description  │         │ description  │
│ stars       │         │ price        │         │ image_url    │
│ review_text │         │ image_url    │         │ display_order│
│ created_at  │         │ thumbnail    │         │ active       │
│ verified    │         │ category_id  │         │ created_at   │
└─────────────┘         │ available    │         └──────────────┘
      │                 │ avg_rating   │
      │*                │ rating_count │
      │                 │ created_at   │
      │1                └──────────────┘
┌─────────────┐
│    Cart     │
│─────────────│
│ id (PK)     │
│ user_id(FK) │
│ created_at  │
│ updated_at  │
└─────────────┘
      │1
      │
      │*
┌─────────────┐
│  CartItem   │
│─────────────│
│ id (PK)     │
│ cart_id(FK) │
│ food_item_id│
│ quantity    │
│ added_at    │
└─────────────┘
```

## Core Entities

### 1. User

Represents authenticated users (customers and admins) via OAuth2 social login.

**JPA Entity**:
```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_provider_external", columnList = "provider,external_id")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider; // GOOGLE, FACEBOOK
    
    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId; // Provider's user ID
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role; // CUSTOMER, ADMIN
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status; // ACTIVE, DEACTIVATED, DELETED
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Rating> ratings = new ArrayList<>();
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;
}
```

**Attributes**:
- `id`: Primary key (auto-increment)
- `email`: Unique email from OAuth2 provider
- `name`: Display name from provider
- `avatarUrl`: Profile picture URL from provider
- `provider`: OAuth2 provider (GOOGLE, FACEBOOK)
- `externalId`: User ID from provider (for profile sync)
- `role`: Access level (CUSTOMER, ADMIN)
- `status`: Account status (ACTIVE, DEACTIVATED, DELETED - soft delete)
- `createdAt`: Registration timestamp
- `updatedAt`: Last profile update

**Relationships**:
- One-to-many with `Order` (customer's orders)
- One-to-many with `Rating` (customer's reviews)
- One-to-one with `Cart` (customer's shopping cart)

**Validation Rules**:
- Email must be valid format
- Name length: 1-255 characters
- Provider and externalId together must be unique

**Indexes**:
- `email` (unique, for login lookups)
- Composite: `(provider, external_id)` (for OAuth2 user mapping)

---

### 2. Category

Represents food item categories for catalog organization.

**JPA Entity**:
```java
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_active_order", columnList = "active,display_order")
})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "image_url", length = 512)
    private String imageUrl; // Optional category image
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder; // For sorting
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<FoodItem> foodItems = new ArrayList<>();
}
```

**Attributes**:
- `id`: Primary key
- `name`: Category name (e.g., "Pizza", "Burgers", "Desserts")
- `description`: Optional category description
- `imageUrl`: Optional category banner image (MinIO)
- `displayOrder`: Integer for custom ordering (lower = higher priority)
- `active`: Soft delete flag
- `createdAt`: Creation timestamp
- `updatedAt`: Last modification timestamp

**Relationships**:
- One-to-many with `FoodItem` (items in this category)

**Validation Rules**:
- Name unique, length: 1-100 characters
- Display order must be non-negative

**Indexes**:
- Composite: `(active, display_order)` (for fast category listing)

---

### 3. FoodItem

Represents products available for purchase.

**JPA Entity**:
```java
@Entity
@Table(name = "food_items", indexes = {
    @Index(name = "idx_food_category", columnList = "category_id"),
    @Index(name = "idx_food_available", columnList = "available"),
    @Index(name = "idx_food_rating", columnList = "avg_rating DESC")
})
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "image_url", length = 512)
    private String imageUrl; // Full-size image (MinIO)
    
    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl; // 200x200 thumbnail
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(nullable = false)
    private Boolean available = true;
    
    @Column(name = "avg_rating", precision = 2, scale = 1)
    private BigDecimal avgRating; // Cached average (1.0-5.0)
    
    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0; // Total ratings
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "foodItem", cascade = CascadeType.ALL)
    private List<Rating> ratings = new ArrayList<>();
    
    @OneToMany(mappedBy = "foodItem", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();
}
```

**Attributes**:
- `id`: Primary key
- `name`: Food item name
- `description`: Detailed description (ingredients, allergens, etc.)
- `price`: Price in USD (2 decimal places)
- `imageUrl`: Full-resolution image URL (MinIO)
- `thumbnailUrl`: Optimized 200x200 thumbnail
- `category`: Foreign key to Category
- `available`: Availability flag (admin can hide items)
- `avgRating`: Cached average rating (denormalized for performance)
- `ratingCount`: Total number of ratings
- `createdAt`: Creation timestamp
- `updatedAt`: Last modification timestamp

**Relationships**:
- Many-to-one with `Category`
- One-to-many with `Rating` (customer reviews)
- One-to-many with `OrderItem` (orders containing this item)

**Validation Rules**:
- Name length: 1-200 characters
- Price > 0
- avgRating: 1.0-5.0 (if not null)

**Indexes**:
- `category_id` (for category browsing)
- `available` (partial index for active items)
- `avg_rating DESC` (for sorting by popularity)

**Cache Strategy**:
- Cache `avgRating` and `ratingCount` to avoid aggregation query on every page load
- Update cache when new rating submitted (async or trigger)

---

### 4. Cart

Represents a customer's shopping cart (ephemeral, stored in session).

**JPA Entity**:
```java
@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id", unique = true)
})
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}
```

**Attributes**:
- `id`: Primary key
- `user`: One-to-one with User (each user has one cart)
- `createdAt`: Cart creation timestamp
- `updatedAt`: Last item added/removed

**Relationships**:
- One-to-one with `User`
- One-to-many with `CartItem` (items in cart)

**Notes**:
- Cart is primarily stored in Redis session for performance
- Database persistence is backup (for cart recovery across sessions)
- Cart expiry: 30 days of inactivity (cleanup job)

---

### 5. CartItem

Represents individual items in a shopping cart.

**JPA Entity**:
```java
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
    @Index(name = "idx_cart_item_food", columnList = "food_item_id")
})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;
    
    @Column(nullable = false)
    private Integer quantity; // 1-99
    
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Attributes**:
- `id`: Primary key
- `cart`: Foreign key to Cart
- `foodItem`: Foreign key to FoodItem
- `quantity`: Number of items (1-99)
- `addedAt`: Timestamp when item was added
- `updatedAt`: Last quantity change

**Validation Rules**:
- Quantity: 1-99
- Unique constraint: `(cart_id, food_item_id)` - no duplicate items in cart

**Indexes**:
- `cart_id` (for loading cart items)
- `food_item_id` (for checking if item already in cart)

---

### 6. Order

Represents a completed purchase.

**JPA Entity**:
```java
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created", columnList = "created_at DESC")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status; // PENDING, CONFIRMED, PREPARING, DELIVERED, CANCELLED
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "stripe_payment_intent_id", length = 255)
    private String stripePaymentIntentId; // For payment reconciliation
    
    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;
    
    @Column(name = "delivery_phone", length = 20)
    private String deliveryPhone;
    
    @Column(name = "notes", length = 1000)
    private String notes; // Customer notes
    
    @Version
    private Long version; // Optimistic locking
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
```

**Attributes**:
- `id`: Primary key
- `user`: Foreign key to User (customer)
- `status`: Order lifecycle stage (PENDING → CONFIRMED → PREPARING → DELIVERED → CANCELLED)
- `totalAmount`: Total order cost (sum of order items)
- `stripePaymentIntentId`: Stripe payment ID for reconciliation
- `deliveryAddress`: Delivery address (text field for MVP)
- `deliveryPhone`: Contact phone number
- `notes`: Optional customer notes
- `version`: Optimistic locking version (prevent concurrent updates)
- `createdAt`: Order placement timestamp
- `updatedAt`: Last status change
- `confirmedAt`: When order was confirmed
- `deliveredAt`: When order was delivered

**Relationships**:
- Many-to-one with `User`
- One-to-many with `OrderItem` (items in this order)

**Validation Rules**:
- totalAmount > 0
- deliveryPhone matches format (simple regex)

**Indexes**:
- `user_id` (for customer order history)
- `status` (for admin order filtering)
- `created_at DESC` (for recent orders listing)

**State Machine**:
```
PENDING ──payment success──> CONFIRMED ──admin──> PREPARING ──admin──> DELIVERED
   │                                          │
   └──payment failure or timeout─> CANCELLED ┘
```

---

### 7. OrderItem

Represents individual items in an order (snapshot at purchase time).

**JPA Entity**:
```java
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_food", columnList = "food_item_id")
})
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase; // Snapshot of food item price
    
    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName; // Snapshot of food item name
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal; // quantity * priceAtPurchase
}
```

**Attributes**:
- `id`: Primary key
- `order`: Foreign key to Order
- `foodItem`: Foreign key to FoodItem (for reference, even if item later deleted)
- `quantity`: Number ordered
- `priceAtPurchase`: Price snapshot (food item price may change later)
- `foodName`: Name snapshot (food item may be renamed/deleted)
- `subtotal`: Cached subtotal (quantity × priceAtPurchase)

**Rationale for Snapshots**:
- Order history must reflect prices at time of purchase (not current prices)
- Order details must remain intact even if food item is deleted

**Indexes**:
- `order_id` (for loading order details)
- `food_item_id` (for food item sales statistics)

---

### 8. Rating

Represents customer reviews and ratings for food items.

**JPA Entity**:
```java
@Entity
@Table(name = "ratings", indexes = {
    @Index(name = "idx_rating_food_item", columnList = "food_item_id"),
    @Index(name = "idx_rating_user_food", columnList = "user_id,food_item_id", unique = true),
    @Index(name = "idx_rating_created", columnList = "created_at DESC")
})
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;
    
    @Column(nullable = false)
    private Integer stars; // 1-5
    
    @Column(name = "review_text", length = 2000)
    private String reviewText; // Optional review
    
    @Column(nullable = false)
    private Boolean verified = false; // True if user actually ordered this item
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Attributes**:
- `id`: Primary key
- `user`: Foreign key to User (reviewer)
- `foodItem`: Foreign key to FoodItem (reviewed item)
- `stars`: Rating value (1-5 stars)
- `reviewText`: Optional text review (max 2000 chars)
- `verified`: True if user has ordered this item (prevents fake reviews)
- `createdAt`: Review submission timestamp
- `updatedAt`: Last edit timestamp

**Validation Rules**:
- stars: 1-5
- Unique constraint: `(user_id, food_item_id)` - one rating per user per item
- reviewText max length: 2000 characters

**Indexes**:
- `food_item_id` (for loading item reviews)
- Unique composite: `(user_id, food_item_id)` (one rating per user per item)
- `created_at DESC` (for recent reviews)

**Business Rules**:
- After rating submission, update `FoodItem.avgRating` and `FoodItem.ratingCount`
- Formula: `avgRating = SUM(stars) / COUNT(*)` across all ratings for food item
- Use async job or database trigger for cache update

---

## Supporting Tables (Enums)

### AuthProvider (Enum)

```java
public enum AuthProvider {
    GOOGLE,
    FACEBOOK
}
```

---

### UserRole (Enum)

```java
public enum UserRole {
    CUSTOMER,
    ADMIN
}
```

---

### UserStatus (Enum)

```java
public enum UserStatus {
    ACTIVE,       // Normal account
    DEACTIVATED,  // Temporarily disabled (admin action)
    DELETED       // Soft deleted (GDPR compliance)
}
```

---

### OrderStatus (Enum)

```java
public enum OrderStatus {
    PENDING,      // Payment initiated but not confirmed
    CONFIRMED,    // Payment successful, order confirmed
    PREPARING,    // Kitchen/restaurant is preparing order
    DELIVERED,    // Order delivered to customer
    CANCELLED     // Order cancelled (payment failed or admin action)
}
```

---

## Database Migrations (Flyway)

### V1__create_users_table.sql

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(512),
    provider VARCHAR(20) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uc_provider_external UNIQUE (provider, external_id)
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_provider_external ON users(provider, external_id);
```

---

### V2__create_categories_table.sql

```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url VARCHAR(512),
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_category_active_order ON categories(active, display_order);
```

---

### V3__create_food_items_table.sql

```sql
CREATE TABLE food_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    image_url VARCHAR(512),
    thumbnail_url VARCHAR(512),
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    avg_rating DECIMAL(2,1),
    rating_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_food_category ON food_items(category_id);
CREATE INDEX idx_food_available ON food_items(available) WHERE available = TRUE;
CREATE INDEX idx_food_rating ON food_items(avg_rating DESC NULLS LAST);
```

---

### V4__create_orders_tables.sql

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount > 0),
    stripe_payment_intent_id VARCHAR(255),
    delivery_address VARCHAR(500),
    delivery_phone VARCHAR(20),
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    delivered_at TIMESTAMP
);

CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created ON orders(created_at DESC);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    food_item_id BIGINT NOT NULL REFERENCES food_items(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0 AND quantity <= 99),
    price_at_purchase DECIMAL(10,2) NOT NULL CHECK (price_at_purchase > 0),
    food_name VARCHAR(200) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal > 0)
);

CREATE INDEX idx_order_item_order ON order_items(order_id);
CREATE INDEX idx_order_item_food ON order_items(food_item_id);
```

---

### V5__create_ratings_table.sql

```sql
CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    food_item_id BIGINT NOT NULL REFERENCES food_items(id) ON DELETE CASCADE,
    stars INTEGER NOT NULL CHECK (stars >= 1 AND stars <= 5),
    review_text TEXT,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uc_user_food_rating UNIQUE (user_id, food_item_id)
);

CREATE INDEX idx_rating_food_item ON ratings(food_item_id);
CREATE INDEX idx_rating_created ON ratings(created_at DESC);
```

---

### V6__create_carts_tables.sql

```sql
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_cart_user ON carts(user_id);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    food_item_id BIGINT NOT NULL REFERENCES food_items(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0 AND quantity <= 99),
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uc_cart_food UNIQUE (cart_id, food_item_id)
);

CREATE INDEX idx_cart_item_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_item_food ON cart_items(food_item_id);
```

---

## Data Integrity Rules

### Foreign Key Constraints

1. **User → Order**: `ON DELETE RESTRICT` (cannot delete users with orders)
2. **Category → FoodItem**: `ON DELETE RESTRICT` (must reassign items before deleting category)
3. **FoodItem → OrderItem**: `ON DELETE RESTRICT` (preserve order history)
4. **Order → OrderItem**: `ON DELETE CASCADE` (delete items when order deleted)
5. **User → Rating**: `ON DELETE CASCADE` (delete ratings when user deleted)
6. **FoodItem → Rating**: `ON DELETE CASCADE` (delete ratings when item deleted)
7. **Cart → CartItem**: `ON DELETE CASCADE` (delete items when cart deleted)

### Check Constraints

1. `food_items.price > 0`
2. `order_items.quantity > 0 AND quantity <= 99`
3. `ratings.stars >= 1 AND stars <= 5`
4. `orders.total_amount > 0`

### Unique Constraints

1. `users.email` (global uniqueness)
2. `users(provider, external_id)` (OAuth2 provider mapping)
3. `categories.name` (no duplicate category names)
4. `ratings(user_id, food_item_id)` (one rating per user per item)
5. `cart_items(cart_id, food_item_id)` (no duplicate items in cart)
6. `carts.user_id` (one cart per user)

---

## Performance Considerations

### Query Optimization

1. **Catalog Browsing**:
   ```sql
   SELECT f.* FROM food_items f 
   WHERE f.category_id = ? AND f.available = TRUE 
   ORDER BY f.avg_rating DESC NULLS LAST 
   LIMIT 50;
   ```
   - Uses indexes: `idx_food_category`, `idx_food_available`, `idx_food_rating`

2. **Order History**:
   ```sql
   SELECT o.*, oi.* FROM orders o 
   JOIN order_items oi ON oi.order_id = o.id 
   WHERE o.user_id = ? 
   ORDER BY o.created_at DESC 
   LIMIT 20;
   ```
   - Uses indexes: `idx_order_user`, `idx_order_created`

3. **Food Item Reviews**:
   ```sql
   SELECT r.*, u.name, u.avatar_url FROM ratings r 
   JOIN users u ON u.id = r.user_id 
   WHERE r.food_item_id = ? 
   ORDER BY r.created_at DESC 
   LIMIT 50;
   ```
   - Uses index: `idx_rating_food_item`

### Denormalization

1. **FoodItem.avgRating**: Cache average rating to avoid `AVG()` aggregation on every page load
2. **FoodItem.ratingCount**: Cache count to avoid `COUNT(*)` query
3. **OrderItem.foodName**: Snapshot food name to preserve order history

### Connection Pooling

- HikariCP configuration: `maximum-pool-size = 10` (for dev, scale in production)
- Monitor active connections with Micrometer metrics

---

## Security Considerations

1. **Soft Deletes**: Use `UserStatus.DELETED` instead of `DELETE FROM users` (audit trail, GDPR compliance)
2. **Optimistic Locking**: `@Version` on `Order` prevents concurrent status updates (race conditions)
3. **Audit Logging**: Log admin actions (user deactivation, order cancellation) in application logs
4. **Input Validation**: JPA `@Column` constraints + Bean Validation prevent SQL injection
5. **Sensitive Data**: Never store credit card numbers (Stripe handles PCI compliance)

---

## Testing Strategy

### Database Tests (Testcontainers)

```java
@SpringBootTest
@Testcontainers
class FoodItemRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    
    @Autowired
    private FoodItemRepository foodItemRepository;
    
    @Test
    void findByCategoryAndAvailable_returnsOnlyAvailableItems() {
        // Test index usage and filtering
    }
}
```

### Integration Tests

- Test order creation flow (cart → order → order items)
- Test rating submission and avgRating cache update
- Test concurrent order updates (optimistic locking)

---

## Summary

The data model supports all 6 user stories with proper relationships, indexes, and constraints. Key design decisions:

1. **OAuth2 User Model**: Supports multiple providers (Google, Facebook) with profile sync
2. **Soft Deletes**: Preserve audit trail for users and orders
3. **Denormalized Ratings**: Cache avgRating/ratingCount for performance
4. **Order Snapshots**: Preserve historical prices and names in OrderItem
5. **Optimistic Locking**: Prevent concurrent order modifications
6. **Proper Indexing**: All frequent queries use indexes (tested with `EXPLAIN ANALYZE`)
7. **Referential Integrity**: Foreign keys ensure data consistency

Next steps: API contracts (OpenAPI specification) in Phase 1.
