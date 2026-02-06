# API Contracts: Food Shop Application

**Feature**: Food Shop Application  
**Date**: 2026-02-06  
**Phase**: 1 - API Contract Design

## Overview

This directory contains API contracts for the Food Shop Application. Since we're using **server-side rendering with Thymeleaf**, most user interactions are handled via standard HTTP form submissions and page navigations (GET/POST requests).

However, we define REST API contracts for:
1. **AJAX endpoints** (cart operations, ratings)
2. **Admin API** (management operations)
3. **Webhook endpoints** (Stripe payment notifications)
4. **Future API consumption** (mobile app, third-party integrations)

## API Design Principles

1. **RESTful Conventions**: Use standard HTTP methods (GET, POST, PUT, DELETE)
2. **JSON Responses**: All AJAX/API endpoints return JSON
3. **Error Handling**: Consistent error response format
4. **Authentication**: Session-based auth for web, token-based for API (future)
5. **Versioning**: `/api/v1/` prefix for future compatibility

## Contract Files

- [openapi.yaml](openapi.yaml) - OpenAPI 3.0 specification for REST endpoints
- This document provides additional context and usage examples

---

## API Endpoints Summary

### Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Home page (Thymeleaf) |
| GET | `/food` | Browse food catalog (Thymeleaf) |
| GET | `/food/{id}` | Food item details (Thymeleaf) |
| GET | `/category/{id}` | Category food items (Thymeleaf) |
| GET | `/auth/oauth2/google` | Initiate Google OAuth2 login |
| GET | `/auth/oauth2/facebook` | Initiate Facebook OAuth2 login |

### Authenticated User Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/cart` | View shopping cart (Thymeleaf) |
| POST | `/api/v1/cart/add` | Add item to cart (AJAX) |
| PUT | `/api/v1/cart/item/{id}` | Update cart item quantity (AJAX) |
| DELETE | `/api/v1/cart/item/{id}` | Remove item from cart (AJAX) |
| GET | `/checkout` | Checkout page (Thymeleaf) |
| POST | `/api/v1/orders` | Create order and initiate payment |
| GET | `/orders` | Order history (Thymeleaf) |
| GET | `/orders/{id}` | Order details (Thymeleaf) |
| POST | `/api/v1/ratings` | Submit rating (AJAX) |
| PUT | `/api/v1/ratings/{id}` | Update rating (AJAX) |
| GET | `/api/v1/food/{id}/share` | Generate share link (AJAX) |

### Admin Endpoints (Role: ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/dashboard` | Admin dashboard (Thymeleaf) |
| GET | `/admin/users` | User management (Thymeleaf) |
| PUT | `/api/v1/admin/users/{id}/status` | Update user status (AJAX) |
| GET | `/admin/categories` | Category management (Thymeleaf) |
| POST | `/api/v1/admin/categories` | Create category |
| PUT | `/api/v1/admin/categories/{id}` | Update category |
| DELETE | `/api/v1/admin/categories/{id}` | Delete category |
| GET | `/admin/orders` | Order management (Thymeleaf) |
| PUT | `/api/v1/admin/orders/{id}/status` | Update order status |
| GET | `/admin/statistics` | Statistics dashboard (Thymeleaf) |
| GET | `/api/v1/admin/statistics/food-items` | Food item statistics (JSON) |

### Webhook Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/webhooks/stripe` | Stripe payment webhook |

---

## Request/Response Examples

### 1. Add Item to Cart (AJAX)

**Request**:
```http
POST /api/v1/cart/add
Content-Type: application/json
Cookie: JSESSIONID=...

{
  "foodItemId": 123,
  "quantity": 2
}
```

**Response (Success)**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Item added to cart",
  "cart": {
    "itemCount": 3,
    "totalAmount": 45.50,
    "items": [
      {
        "id": 1,
        "foodItemId": 123,
        "foodName": "Margherita Pizza",
        "quantity": 2,
        "pricePerItem": 12.99,
        "subtotal": 25.98,
        "thumbnailUrl": "https://cdn.example.com/pizza-thumb.jpg"
      }
    ]
  }
}
```

**Response (Error - Item Unavailable)**:
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "success": false,
  "error": {
    "code": "ITEM_UNAVAILABLE",
    "message": "This item is currently unavailable",
    "field": "foodItemId"
  }
}
```

---

### 2. Update Cart Item Quantity (AJAX)

**Request**:
```http
PUT /api/v1/cart/item/1
Content-Type: application/json
Cookie: JSESSIONID=...

{
  "quantity": 3
}
```

**Response (Success)**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Cart updated",
  "cart": {
    "itemCount": 4,
    "totalAmount": 58.49
  }
}
```

---

### 3. Remove Item from Cart (AJAX)

**Request**:
```http
DELETE /api/v1/cart/item/1
Cookie: JSESSIONID=...
```

**Response (Success)**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Item removed from cart",
  "cart": {
    "itemCount": 1,
    "totalAmount": 32.51
  }
}
```

---

### 4. Create Order (AJAX)

**Request**:
```http
POST /api/v1/orders
Content-Type: application/json
Cookie: JSESSIONID=...

{
  "deliveryAddress": "123 Main St, Apt 4B, New York, NY 10001",
  "deliveryPhone": "+1-555-123-4567",
  "notes": "Please ring doorbell"
}
```

**Response (Success - Redirect to Stripe)**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "orderId": 456,
  "stripeCheckoutUrl": "https://checkout.stripe.com/pay/cs_test_abc123xyz",
  "message": "Redirecting to payment..."
}
```

**Flow**:
1. Client POSTs order details
2. Server creates Order (status: PENDING)
3. Server creates Stripe Checkout Session
4. Server returns Stripe checkout URL
5. Client redirects to Stripe
6. Customer completes payment on Stripe
7. Stripe webhook calls `/api/v1/webhooks/stripe`
8. Server updates Order (status: CONFIRMED)
9. Server sends email notifications

---

### 5. Submit Rating (AJAX)

**Request**:
```http
POST /api/v1/ratings
Content-Type: application/json
Cookie: JSESSIONID=...

{
  "foodItemId": 123,
  "stars": 5,
  "reviewText": "Best pizza I've ever had! Crispy crust, fresh ingredients."
}
```

**Response (Success)**:
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "success": true,
  "message": "Rating submitted successfully",
  "rating": {
    "id": 789,
    "foodItemId": 123,
    "stars": 5,
    "reviewText": "Best pizza I've ever had! Crispy crust, fresh ingredients.",
    "verified": true,
    "createdAt": "2026-02-06T14:30:00Z"
  },
  "foodItem": {
    "avgRating": 4.7,
    "ratingCount": 42
  }
}
```

**Response (Error - Already Rated)**:
```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "success": false,
  "error": {
    "code": "RATING_EXISTS",
    "message": "You have already rated this item. Use PUT to update your rating.",
    "field": "foodItemId"
  }
}
```

---

### 6. Update User Status (Admin AJAX)

**Request**:
```http
PUT /api/v1/admin/users/123/status
Content-Type: application/json
Cookie: JSESSIONID=...

{
  "status": "DEACTIVATED",
  "reason": "Spam/abuse reports"
}
```

**Response (Success)**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "User status updated",
  "user": {
    "id": 123,
    "email": "user@example.com",
    "status": "DEACTIVATED",
    "updatedAt": "2026-02-06T14:35:00Z"
  }
}
```

---

### 7. Update Order Status (Admin AJAX)

**Request**:
```http
PUT /api/v1/admin/orders/456/status
Content-Type: application/json
Cookie: JSESSIONID=...

{
  "status": "PREPARING",
  "notifyCustomer": true
}
```

**Response (Success)**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Order status updated. Email notification sent to customer.",
  "order": {
    "id": 456,
    "status": "PREPARING",
    "updatedAt": "2026-02-06T14:40:00Z"
  }
}
```

---

### 8. Create Category (Admin API)

**Request**:
```http
POST /api/v1/admin/categories
Content-Type: application/json
Cookie: JSESSIONID=...

{
  "name": "Desserts",
  "description": "Sweet treats and desserts",
  "displayOrder": 5
}
```

**Response (Success)**:
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "success": true,
  "message": "Category created",
  "category": {
    "id": 10,
    "name": "Desserts",
    "description": "Sweet treats and desserts",
    "displayOrder": 5,
    "active": true,
    "createdAt": "2026-02-06T14:45:00Z"
  }
}
```

---

### 9. Get Food Item Statistics (Admin API)

**Request**:
```http
GET /api/v1/admin/statistics/food-items?startDate=2026-01-01&endDate=2026-02-06
Cookie: JSESSIONID=...
```

**Response (Success)**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "period": {
    "startDate": "2026-01-01",
    "endDate": "2026-02-06"
  },
  "summary": {
    "totalRevenue": 12450.75,
    "totalOrders": 542,
    "avgOrderValue": 22.97
  },
  "topItems": [
    {
      "id": 123,
      "name": "Margherita Pizza",
      "orders": 87,
      "revenue": 1129.13,
      "avgRating": 4.7,
      "ratingCount": 42
    },
    {
      "id": 456,
      "name": "Cheeseburger",
      "orders": 65,
      "revenue": 843.50,
      "avgRating": 4.5,
      "ratingCount": 31
    }
  ]
}
```

---

### 10. Stripe Webhook (Payment Confirmation)

**Request** (from Stripe):
```http
POST /api/v1/webhooks/stripe
Content-Type: application/json
Stripe-Signature: t=1612137600,v1=abc123...

{
  "id": "evt_1abc123",
  "type": "checkout.session.completed",
  "data": {
    "object": {
      "id": "cs_test_abc123",
      "payment_intent": "pi_abc123",
      "client_reference_id": "456",
      "payment_status": "paid"
    }
  }
}
```

**Response (Success)**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "received": true
}
```

**Server Actions**:
1. Verify Stripe signature (security)
2. Extract `client_reference_id` (Order ID)
3. Update Order status: PENDING → CONFIRMED
4. Send email notifications (async)
5. Clear customer's cart

---

## Error Response Format

All API errors follow consistent format:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "field": "fieldName",
    "details": {}
  }
}
```

**Common Error Codes**:
- `VALIDATION_ERROR`: Request validation failed
- `AUTHENTICATION_REQUIRED`: User not logged in
- `AUTHORIZATION_FAILED`: User lacks permission
- `RESOURCE_NOT_FOUND`: Requested resource doesn't exist
- `ITEM_UNAVAILABLE`: Food item not available
- `RATING_EXISTS`: User already rated item
- `PAYMENT_FAILED`: Stripe payment failed
- `INTERNAL_ERROR`: Server error (logged with details)

**HTTP Status Codes**:
- `200 OK`: Success (GET, PUT, DELETE)
- `201 Created`: Resource created (POST)
- `400 Bad Request`: Validation error
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Authorization failed
- `404 Not Found`: Resource not found
- `409 Conflict`: Duplicate resource (e.g., rating already exists)
- `500 Internal Server Error`: Server error

---

## Authentication & Authorization

### Session-Based Auth (Web)

- Spring Security manages sessions in Redis
- Session cookie: `JSESSIONID` (HttpOnly, Secure, SameSite=Lax)
- Session timeout: 30 minutes of inactivity
- CSRF protection: Token in form/meta tag

### OAuth2 Flow

1. User clicks "Sign in with Google/Facebook"
2. Redirect to provider: `/oauth2/authorization/{provider}`
3. User authenticates on provider
4. Provider redirects back: `/login/oauth2/code/{provider}`
5. Spring Security OAuth2 Client exchanges code for tokens
6. Custom `OAuth2UserService` creates/updates local `User` entity
7. Session created, user redirected to home page

### Role-Based Access Control

- `CUSTOMER`: Access to browse, order, rate, share
- `ADMIN`: Access to user/category/order management, statistics

**Spring Security Configuration**:
```java
http
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/food/**", "/category/**").permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
    )
    .oauth2Login(oauth -> oauth
        .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
    )
    .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
```

---

## Rate Limiting

To prevent abuse, implement rate limiting on API endpoints:

### Limits (Per User/IP)

- **Cart operations**: 100 requests/minute
- **Rating submission**: 10 requests/hour
- **Admin operations**: 500 requests/hour
- **Webhook endpoints**: Verified by Stripe signature (no rate limit)

**Implementation**: Spring bucket4j or Redis-based rate limiter

---

## API Versioning Strategy

- **Current**: `/api/v1/` (all endpoints)
- **Future**: `/api/v2/` for breaking changes
- **Backward Compatibility**: Maintain v1 for 6 months after v2 release

**Versioning Approach**:
- URI versioning (simple, explicit)
- Version in path, not query parameter
- Version in Accept header (future consideration for REST purists)

---

## Testing API Contracts

### Contract Tests (Spring MockMvc)

```java
@WebMvcTest(CartController.class)
class CartControllerContractTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CartService cartService;
    
    @Test
    @WithMockUser
    void addToCart_validRequest_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"foodItemId\":123,\"quantity\":2}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.cart.itemCount").exists());
    }
    
    @Test
    @WithMockUser
    void addToCart_itemUnavailable_returns400() throws Exception {
        when(cartService.addItem(any(), anyLong(), anyInt()))
            .thenThrow(new ItemUnavailableException("Item unavailable"));
        
        mockMvc.perform(post("/api/v1/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"foodItemId\":999,\"quantity\":1}")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("ITEM_UNAVAILABLE"));
    }
}
```

### Integration Tests (with Testcontainers)

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderApiIntegrationTest {
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void createOrder_validCart_createsOrderAndReturnsStripeUrl() {
        // Setup test data
        // Authenticate test user
        // Create order
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/v1/orders",
            orderRequest,
            OrderResponse.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStripeCheckoutUrl()).isNotNull();
    }
}
```

---

## Summary

API contracts define:

1. **REST Endpoints**: 20+ endpoints for AJAX operations and admin management
2. **Request/Response Format**: Consistent JSON structure with error handling
3. **Authentication**: Session-based auth (OAuth2) with CSRF protection
4. **Authorization**: Role-based access (CUSTOMER, ADMIN)
5. **Error Handling**: Standardized error codes and HTTP status codes
6. **Rate Limiting**: Abuse prevention on API endpoints
7. **Versioning**: `/api/v1/` with backward compatibility plan
8. **Testing**: Contract tests (MockMvc) and integration tests (Testcontainers)

The API design prioritizes:
- **Simplicity**: RESTful conventions, predictable patterns
- **Security**: CSRF protection, role-based access, rate limiting
- **Performance**: Async email processing, webhook-based payment confirmation
- **Testability**: Clear contracts enable comprehensive testing

Next steps: Quickstart guide (Phase 1) and agent context update.
