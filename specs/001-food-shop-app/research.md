# Research: Food Shop Application

**Feature**: Food Shop Application  
**Date**: 2026-02-06  
**Phase**: 0 - Technical Research & Decision Documentation

## Overview

This document captures technical research and architectural decisions for the Food Shop Application. All technology choices align with the specified stack: Java 21 + Spring Boot 3.2, PostgreSQL, Redis, MinIO, Thymeleaf, OAuth2, Bootstrap 5, Docker Compose, and Stripe.

## Technology Stack Research

### 1. Spring Boot 3.2 + Java 21

**Decision**: Use Spring Boot 3.2.x with Java 21 LTS

**Rationale**:
- Spring Boot 3.2 provides mature ecosystem for web applications with excellent integration support
- Java 21 LTS ensures long-term support (until 2029) with modern language features (virtual threads, pattern matching, records)
- Virtual threads (Project Loom) improve concurrency for I/O-heavy operations (database, Redis, MinIO, Stripe API)
- Built-in support for OAuth2, validation, data access, testing, and email

**Alternatives Considered**:
- **Java 17 LTS**: More conservative choice, but misses virtual threads and newer language features
- **Spring Boot 2.x**: End-of-life approaching, lacks Java 21 support
- **Micronaut/Quarkus**: Lower memory footprint but smaller ecosystem and less mature OAuth2 support

**Best Practices**:
- Use Spring Boot Starters to minimize configuration
- Leverage autoconfiguration for PostgreSQL, Redis, OAuth2
- Use `@ConfigurationProperties` for type-safe configuration
- Enable actuator endpoints for health checks and metrics

---

### 2. PostgreSQL 16

**Decision**: Use PostgreSQL 16 as primary relational database

**Rationale**:
- Excellent Spring Data JPA support with Hibernate ORM
- ACID compliance ensures data consistency for orders and payments
- Advanced indexing (B-tree, GIN for full-text search on food items)
- JSON/JSONB support for flexible rating metadata
- Mature connection pooling with HikariCP (Spring Boot default)

**Schema Strategy**:
- Use Flyway for versioned migrations (better than Liquibase for SQL-first approach)
- Optimistic locking (`@Version`) for orders to prevent concurrent modifications
- Proper foreign key constraints for data integrity
- Indexes on: `food_items.category_id`, `orders.user_id`, `orders.status`, `ratings.food_item_id`

**Alternatives Considered**:
- **MySQL**: Less advanced JSON support, weaker full-text search
- **MongoDB**: Not suitable for transactional order processing

**Best Practices**:
- Use connection pooling (HikariCP) with pool size = (core count × 2) + disk spindles
- Enable query logging in development, disable in production
- Use `EXPLAIN ANALYZE` for query performance tuning
- Implement soft deletes for user and order data (audit trail)

---

### 3. Redis 7 (Session Management)

**Decision**: Use Redis for HTTP session storage with Spring Session

**Rationale**:
- Horizontal scalability: Sessions persist across multiple application instances
- Fast session lookup (<1ms) improves response times
- TTL support automatically expires inactive sessions
- Shopping cart can be stored in session (ephemeral, OK to lose on expiry)
- OAuth2 tokens cached for performance

**Configuration**:
- Session timeout: 30 minutes of inactivity
- Use `spring-session-data-redis` for seamless integration
- Redis persistence: AOF (Append-Only File) for durability

**Alternatives Considered**:
- **In-memory sessions**: Not scalable across multiple instances
- **Database sessions**: Slower, increases database load

**Best Practices**:
- Use Redis Sentinel for high availability in production
- Configure maxmemory policy: `allkeys-lru` to evict least recently used keys
- Monitor Redis memory usage, alert if >80% capacity

---

### 4. MinIO (S3-Compatible File Storage)

**Decision**: Use MinIO for storing food item images

**Rationale**:
- S3-compatible API allows easy migration to AWS S3 if needed
- Self-hosted solution reduces cloud costs
- High performance for image serving
- Built-in access policies for public read on food images

**Storage Strategy**:
- Bucket: `food-images` (public-read policy)
- Bucket: `user-uploads` (private, signed URLs for admin uploads)
- Image naming: `{food_item_id}/{uuid}.{ext}` to prevent collisions
- Store multiple sizes: original, thumbnail (200x200), medium (800x600)

**Alternatives Considered**:
- **Local filesystem**: Not scalable, no CDN integration
- **AWS S3**: Higher cost, introduces cloud dependency

**Best Practices**:
- Serve images via CDN (CloudFlare/CloudFront) for performance
- Implement image validation: max 5MB, formats (JPEG, PNG, WebP)
- Use `multipart/form-data` upload with progress indication
- Generate pre-signed URLs for admin uploads (temporary write access)

---

### 5. Thymeleaf + Bootstrap 5

**Decision**: Server-side rendering with Thymeleaf templates styled with Bootstrap 5

**Rationale**:
- Native Spring Boot integration, no separate frontend build
- SEO-friendly (server-rendered HTML)
- Bootstrap 5 provides responsive, accessible components out-of-box
- Faster time-to-market than SPA (React/Vue)
- Reduced JavaScript complexity aligns with constitution simplicity principle

**Template Structure**:
- Base layout with Thymeleaf fragments for header/footer reuse
- Form handling with Spring validation annotations
- CSRF protection enabled (Spring Security default)
- Use Bootstrap 5 utility classes to minimize custom CSS

**Alternatives Considered**:
- **React SPA**: Overkill for CRUD app, requires separate API backend
- **JSP**: Legacy technology, inferior to Thymeleaf

**Best Practices**:
- Enable Thymeleaf template caching in production (`spring.thymeleaf.cache=true`)
- Use natural templates for designer-developer collaboration
- Implement fragment parameters for reusable components (cards, modals)
- Load Bootstrap 5 from CDN (jsDelivr) to leverage browser caching

---

### 6. OAuth2 (Social Authentication)

**Decision**: Spring Security OAuth2 Client for Google and Facebook login

**Rationale**:
- No password management overhead (reduces security liability)
- Higher conversion rates (users prefer social login)
- Spring Security OAuth2 Client simplifies integration
- Automatic user profile sync from providers

**Implementation Strategy**:
- Use `spring-boot-starter-oauth2-client`
- Configure Google OAuth2 (client ID, secret, redirect URI)
- Configure Facebook OAuth2 (app ID, secret, redirect URI)
- Store OAuth2 tokens in Redis (encrypted)
- Create local `User` entity after first successful OAuth2 login

**User Attributes Mapping**:
- Google: `sub` → `externalId`, `email`, `name`, `picture` → `avatarUrl`
- Facebook: `id` → `externalId`, `email`, `name`, `picture.data.url` → `avatarUrl`

**Alternatives Considered**:
- **Username/password auth**: Requires password reset, email verification overhead
- **SAML**: Enterprise overkill for consumer app

**Best Practices**:
- Never store OAuth2 access tokens in plain text (use Spring Security encryption)
- Implement token refresh logic for long-lived sessions
- Handle OAuth2 errors gracefully (provider down, user denies access)
- Use `@AuthenticationPrincipal` for accessing logged-in user

---

### 7. Stripe Payment Integration

**Decision**: Stripe Java SDK for payment processing

**Rationale**:
- Industry-standard payment gateway with excellent docs
- PCI DSS compliance handled by Stripe (reduces security burden)
- Supports credit cards, digital wallets (Apple Pay, Google Pay)
- Webhook support for async payment confirmation

**Integration Approach**:
- Use Stripe Checkout (hosted payment page) for PCI compliance
- Flow: Create checkout session → Redirect to Stripe → Webhook confirms payment
- Store Stripe `payment_intent_id` in Order entity for reconciliation
- Implement webhook endpoint (`/api/stripe/webhook`) to update order status

**Alternatives Considered**:
- **PayPal**: Less modern API, more complex integration
- **Custom payment processing**: Requires PCI compliance certification

**Best Practices**:
- Always verify webhook signatures to prevent fraud
- Use idempotency keys for payment operations (prevent double charging)
- Implement exponential backoff for Stripe API retries
- Store minimal payment data (no credit card numbers, only Stripe IDs)
- Test with Stripe test mode webhooks

---

### 8. Docker Compose (Local Development)

**Decision**: Docker Compose for local development environment

**Rationale**:
- Consistent development environment across team (eliminates "works on my machine")
- Easy setup: `docker-compose up` starts all services (PostgreSQL, Redis, MinIO)
- Production-like environment with service isolation
- Testcontainers for integration tests use same Docker images

**Services Configuration**:
```yaml
services:
  postgres:
    image: postgres:16-alpine
    volumes: [postgres-data]
    ports: [5432:5432]
    environment: [POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD]
  
  redis:
    image: redis:7-alpine
    ports: [6379:6379]
    command: redis-server --appendonly yes
  
  minio:
    image: minio/minio:latest
    ports: [9000:9000, 9001:9001]
    command: server /data --console-address ":9001"
    volumes: [minio-data]
  
  app:
    build: .
    depends_on: [postgres, redis, minio]
    ports: [8080:8080]
    environment: [SPRING_PROFILES_ACTIVE=dev]
```

**Alternatives Considered**:
- **Local installation**: Painful setup, version conflicts
- **Kubernetes (Minikube)**: Overkill for local development

**Best Practices**:
- Use named volumes for data persistence
- Use `.env` file for secrets (excluded from Git)
- Implement health checks for service readiness
- Use `docker-compose down -v` to reset test data

---

## Architecture Patterns

### 1. Layered Architecture

**Pattern**: Controller → Service → Repository → Database

**Rationale**:
- Clear separation of concerns aligns with constitution code quality standards
- Testable: Mock services in controller tests, mock repositories in service tests
- Standard Spring Boot pattern, well-understood by Java developers

**Layer Responsibilities**:
- **Controller**: HTTP handling, request validation, response formatting (DTOs)
- **Service**: Business logic, transaction management, authorization checks
- **Repository**: Data access, query optimization, entity management
- **Domain**: JPA entities representing database tables

---

### 2. Async Email Processing

**Pattern**: Publish order events to in-memory queue, async worker sends emails

**Rationale**:
- Email sending can take 1-5 seconds (SMTP latency)
- Async processing keeps order confirmation fast (<2s)
- Failures don't block checkout flow (retry logic handles transient errors)

**Implementation**:
- Use Spring `@Async` with `TaskExecutor` for email sending
- Store email status in database (`PENDING`, `SENT`, `FAILED`)
- Retry failed emails with exponential backoff (max 3 attempts)

---

### 3. Caching Strategy

**Pattern**: Multi-level caching (Redis + Caffeine in-memory)

**Cacheable Data**:
- **Food items catalog** (L1: Caffeine 5min TTL, L2: Redis 30min TTL)
- **Category tree** (Caffeine 10min TTL)
- **User profile** (Redis, session TTL)

**Cache Invalidation**:
- **Write-through**: Update cache on food item/category changes (admin operations)
- **Time-based expiry**: Redis TTL handles stale data
- **Cache-aside**: Application checks cache, falls back to database

---

### 4. Database Connection Pooling

**Pattern**: HikariCP (Spring Boot default)

**Configuration**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10  # For dev (2 CPU cores)
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**Rationale**:
- Connection pooling prevents connection exhaustion under load
- HikariCP is fastest JDBC pool, Spring Boot's default

---

## Performance Optimization

### 1. Database Indexing

**Indexes Required**:
```sql
-- Catalog browsing
CREATE INDEX idx_food_items_category ON food_items(category_id);
CREATE INDEX idx_food_items_available ON food_items(available) WHERE available = true;

-- Order queries
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

-- Ratings
CREATE INDEX idx_ratings_food_item ON ratings(food_item_id);
CREATE INDEX idx_ratings_user_item ON ratings(user_id, food_item_id);

-- Full-text search (future)
CREATE INDEX idx_food_items_search ON food_items USING GIN(to_tsvector('english', name || ' ' || description));
```

---

### 2. N+1 Query Prevention

**Pattern**: Use `@EntityGraph` or `JOIN FETCH` to eagerly load associations

**Example**:
```java
@EntityGraph(attributePaths = {"orderItems", "orderItems.foodItem"})
@Query("SELECT o FROM Order o WHERE o.user = :user")
List<Order> findByUserWithItems(@Param("user") User user);
```

**Rationale**:
- Prevents N+1 queries when loading order items
- Constitution principle IV prohibits N+1 queries

---

### 3. CDN for Static Assets

**Pattern**: Serve food images via CDN (CloudFlare) pointing to MinIO

**Configuration**:
- MinIO bucket: Public-read policy
- CloudFlare origin: MinIO endpoint (`http://minio:9000/food-images`)
- Cache TTL: 30 days for images (immutable URLs with hash)

**Rationale**:
- Reduces server load, improves page load times
- Meets constitution FCP < 1.5s requirement

---

## Security Considerations

### 1. CSRF Protection

**Pattern**: Spring Security CSRF tokens for all POST/PUT/DELETE requests

**Implementation**:
- Thymeleaf auto-includes CSRF token in forms (`<form th:action="...">`)
- Ajax requests include token from meta tag

---

### 2. SQL Injection Prevention

**Pattern**: Always use parameterized queries (Spring Data JPA default)

**Never**:
```java
// NEVER DO THIS
String query = "SELECT * FROM users WHERE email = '" + email + "'";
```

**Always**:
```java
// Spring Data JPA (safe)
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);
```

---

### 3. OAuth2 Token Security

**Pattern**: Encrypt OAuth2 tokens in Redis

**Configuration**:
```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          google:
            issuer-uri: https://accounts.google.com
```

**Rationale**:
- OAuth2 tokens grant access to user's social media account
- Encryption prevents token theft from Redis breach

---

## Testing Strategy

### 1. Unit Tests (JUnit 5)

**Scope**: Service layer business logic

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private EmailService emailService;
    @InjectMocks private OrderService orderService;
    
    @Test
    void createOrder_sendsEmailAsync() {
        // Test order creation triggers email
    }
}
```

**Coverage Target**: 80% line coverage (constitution requirement)

---

### 2. Integration Tests (Testcontainers)

**Scope**: Database, Redis, MinIO integration

**Example**:
```java
@SpringBootTest
@Testcontainers
class OrderFlowIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);
    
    @Test
    void completeOrderFlow_persistsToDatabase() {
        // Full order flow with real database
    }
}
```

---

### 3. End-to-End Tests (Selenium)

**Scope**: Critical user journeys (US1: Browse & Order, US2: Rate & Share)

**Example**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class OrderJourneyTest {
    private WebDriver driver;
    
    @Test
    void customerCanBrowseAndOrder() {
        driver.get("http://localhost:8080");
        // Navigate catalog, add to cart, checkout
    }
}
```

---

## Monitoring & Observability

### 1. Metrics (Micrometer + Prometheus)

**Metrics to Track**:
- HTTP request latency (p50, p95, p99)
- Database query time
- Cache hit rate (Caffeine, Redis)
- Order creation rate
- Stripe payment success rate

**Configuration**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

### 2. Logging (Logback + JSON)

**Log Levels**:
- **ERROR**: Payment failures, email send failures, OAuth2 errors
- **WARN**: Cache misses, slow queries (>100ms)
- **INFO**: Order created, user registered, admin actions
- **DEBUG**: OAuth2 token refresh, cache operations (dev only)

**Structured Logging**:
```java
log.info("Order created", 
    kv("orderId", order.getId()), 
    kv("userId", user.getId()), 
    kv("totalAmount", order.getTotal()));
```

---

## Deployment Considerations

### 1. Docker Image Optimization

**Multi-stage Build**:
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Rationale**:
- Build stage includes Maven, runtime stage only JRE (smaller image)
- Layer caching speeds up rebuilds

---

### 2. Environment Configuration

**Profiles**:
- `dev`: Local development (Docker Compose, verbose logging)
- `prod`: Production (external PostgreSQL, Redis, MinIO, structured JSON logging)

**Externalized Config**:
- Use environment variables for secrets (not `application.yml`)
- Example: `SPRING_DATASOURCE_PASSWORD`, `OAUTH2_GOOGLE_CLIENT_SECRET`

---

## Summary

All technology choices align with the provided stack and constitution principles:
- **Code Quality**: Checkstyle + SpotBugs for linting, Google Java Style Guide
- **Testing**: JUnit 5 + Testcontainers + Selenium, TDD workflow, 80% coverage target
- **UX**: Bootstrap 5 for responsive design, Thymeleaf for server-side rendering, WCAG 2.1 AA compliance
- **Performance**: Redis caching, database indexing, CDN for images, connection pooling, async email processing

Next steps: Phase 1 (data model, API contracts, quickstart guide).
