# Implementation Plan: Food Shop Application

**Branch**: `001-food-shop-app` | **Date**: 2026-02-06 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-food-shop-app/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build a full-stack food shop e-commerce application with social authentication, shopping cart, order management, ratings/reviews, social sharing, and comprehensive admin dashboard. Tech stack: Java 21 + Spring Boot 3.2 backend, PostgreSQL for persistence, Redis for session management, MinIO for file storage, Thymeleaf for server-side rendering with Bootstrap 5 UI, OAuth2 for social authentication, Stripe for payments, all orchestrated via Docker Compose for local development.

## Technical Context

**Language/Version**: Java 21 LTS (Spring Boot 3.2.x)
**Primary Dependencies**: Spring Boot (Web, Security, Data JPA, OAuth2 Client, Mail, Validation), Thymeleaf, Bootstrap 5, Stripe Java SDK
**Storage**: PostgreSQL 16 (primary database), Redis 7 (session store), MinIO (S3-compatible file storage for images)
**Testing**: JUnit 5, Spring Boot Test, Testcontainers (PostgreSQL, Redis, MinIO), MockMvc, Selenium (E2E)
**Target Platform**: Linux server (containerized deployment), Docker Compose for local development
**Project Type**: Monolithic web application (single Spring Boot application)
**Performance Goals**: 
  - API p95 < 300ms for catalog reads, < 500ms for order operations
  - Page load FCP < 1.5s, TTI < 3.5s, LCP < 2.5s
  - Support 500 concurrent users
  - Order submission < 2s end-to-end (async email processing)
**Constraints**: 
  - Server-side rendering only (no SPA framework)
  - Bootstrap 5 for UI consistency (no custom CSS framework)
  - Stripe payment integration required
  - OAuth2 social login (Google, Facebook) mandatory
  - All services must run in Docker containers
**Scale/Scope**: 
  - MVP targeting 500-1000 concurrent users
  - Catalog: ~500-1000 food items initially
  - Expected: 100-200 orders/day at launch
  - Admin users: 5-10 initially

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Initial Check (Before Phase 0)

- [x] **Code Quality**: Checkstyle + SpotBugs configured, Google Java Style Guide adopted
- [x] **Testing Standards**: JUnit 5 + Testcontainers for integration tests, TDD workflow planned
- [x] **UX Consistency**: Bootstrap 5 provides consistent patterns, WCAG 2.1 AA compliance required, Thymeleaf fragments for reusable components
- [x] **Performance Requirements**: Database indexing strategy defined, Redis caching planned, CDN for MinIO assets, Micrometer metrics for monitoring
- [x] **Review Process**: GitHub PR template includes constitution compliance checklist

*All gates pass. No complexity violations require justification.*

### Post-Design Check (After Phase 1)

**Re-evaluation Date**: 2026-02-06

- [x] **Code Quality Standards** (✅ PASS):
  - Maven project structure follows Spring Boot conventions
  - Checkstyle configuration in `pom.xml` enforces Google Java Style Guide
  - SpotBugs configured for static analysis
  - Package structure: domain, repository, service, controller follows layered architecture
  - No code smells identified in design (proper separation of concerns)

- [x] **Testing Standards** (✅ PASS):
  - **Unit Tests**: JUnit 5 for service layer (e.g., `FoodItemServiceTest`, `OrderServiceTest`)
  - **Integration Tests**: Testcontainers for repository layer (PostgreSQL, Redis, MinIO)
  - **E2E Tests**: Selenium tests for critical user flows (browse → cart → checkout)
  - **Contract Tests**: MockMvc for REST API endpoints
  - **Coverage**: JaCoCo configured with 80% line coverage threshold (constitution requirement)
  - **TDD Workflow**: Documented in `quickstart.md` (write test → red → green → refactor)

- [x] **UX Consistency** (✅ PASS):
  - **Consistent Patterns**: Bootstrap 5 components (cards, forms, modals) used throughout
  - **Thymeleaf Fragments**: Reusable header/footer/navigation in `templates/fragments/`
  - **WCAG 2.1 AA Compliance**:
    - Semantic HTML5 elements (`<nav>`, `<main>`, `<article>`)
    - ARIA labels for interactive elements (cart buttons, ratings)
    - Keyboard navigation support (tab order, focus indicators)
    - Color contrast ratios meet AA standards (Bootstrap default theme)
  - **Responsive Design**: Bootstrap grid system (mobile-first, tested on 320px-1920px)
  - **Loading States**: Spinner components for async operations (order submission)

- [x] **Performance Requirements** (✅ PASS):
  - **Database Optimization**:
    - Indexes defined in `data-model.md`: `idx_category_display_order`, `idx_food_items_category`, `idx_orders_user_status`, `idx_ratings_food_item`
    - N+1 query prevention: `@EntityGraph` annotations for JPA entities (e.g., `Order` → `OrderItem` fetch)
    - Connection pooling: HikariCP (default in Spring Boot) with max pool size 20
  - **Caching Strategy**:
    - Redis: Session data (Spring Session), catalog categories (30min TTL)
    - HTTP caching: ETags for food item images, Cache-Control headers
    - Database query cache: Hibernate second-level cache disabled (prefer Redis)
  - **CDN for Assets**: MinIO public bucket for images, CloudFront integration planned for production
  - **API Performance Goals**: Documented in `research.md` (catalog reads <300ms p95, order ops <500ms p95)
  - **Monitoring**: Micrometer + Prometheus metrics exposed at `/actuator/metrics`

- [x] **Dependency Management** (✅ PASS):
  - All dependencies specified in `pom.xml` with explicit versions
  - Spring Boot BOM manages transitive dependencies
  - No deprecated APIs used (Java 21 + Spring Boot 3.2.x)

**Final Verdict**: ✅ **ALL GATES PASS**

No constitution violations identified in Phase 1 design. Architecture supports all quality gates:
- Layered architecture enables testability (80% coverage achievable)
- Bootstrap 5 + Thymeleaf ensures UX consistency
- Database indexes + Redis caching meet performance requirements
- Google Java Style Guide + Checkstyle enforce code quality

**Proceed to Phase 2** (task breakdown via `/speckit.tasks` command).

## Project Structure

### Documentation (this feature)

```text
specs/001-food-shop-app/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── openapi.yaml     # REST API specification
│   └── README.md        # API documentation guide
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
food-shop/
├── docker-compose.yml           # Local development orchestration
├── .env.example                 # Environment variables template
├── pom.xml                      # Maven configuration
├── checkstyle.xml               # Code quality rules
├── .gitignore                   # Git ignore patterns
│
├── src/
│   ├── main/
│   │   ├── java/com/foodshop/
│   │   │   ├── FoodShopApplication.java         # Spring Boot main class
│   │   │   │
│   │   │   ├── config/                          # Configuration
│   │   │   │   ├── SecurityConfig.java          # Spring Security + OAuth2
│   │   │   │   ├── RedisConfig.java             # Session management
│   │   │   │   ├── MinIOConfig.java             # File storage
│   │   │   │   ├── StripeConfig.java            # Payment gateway
│   │   │   │   └── WebMvcConfig.java            # MVC configuration
│   │   │   │
│   │   │   ├── domain/                          # Domain entities (JPA)
│   │   │   │   ├── User.java
│   │   │   │   ├── FoodItem.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Rating.java
│   │   │   │   ├── Cart.java
│   │   │   │   └── CartItem.java
│   │   │   │
│   │   │   ├── repository/                      # Data access (Spring Data JPA)
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── FoodItemRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── RatingRepository.java
│   │   │   │   └── CartRepository.java
│   │   │   │
│   │   │   ├── service/                         # Business logic
│   │   │   │   ├── UserService.java
│   │   │   │   ├── FoodItemService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── RatingService.java
│   │   │   │   ├── CartService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── FileStorageService.java      # MinIO integration
│   │   │   │   ├── PaymentService.java          # Stripe integration
│   │   │   │   └── ShareService.java            # Social sharing
│   │   │   │
│   │   │   ├── controller/                      # MVC controllers
│   │   │   │   ├── HomeController.java          # Public pages
│   │   │   │   ├── AuthController.java          # OAuth2 callbacks
│   │   │   │   ├── FoodItemController.java      # Catalog browsing
│   │   │   │   ├── CartController.java          # Shopping cart
│   │   │   │   ├── OrderController.java         # Order management
│   │   │   │   ├── RatingController.java        # Ratings & reviews
│   │   │   │   └── admin/                       # Admin controllers
│   │   │   │       ├── AdminDashboardController.java
│   │   │   │       ├── AdminUserController.java
│   │   │   │       ├── AdminCategoryController.java
│   │   │   │       ├── AdminOrderController.java
│   │   │   │       └── AdminStatisticsController.java
│   │   │   │
│   │   │   ├── dto/                             # Data Transfer Objects
│   │   │   │   ├── FoodItemDto.java
│   │   │   │   ├── OrderDto.java
│   │   │   │   ├── RatingDto.java
│   │   │   │   └── StatisticsDto.java
│   │   │   │
│   │   │   ├── security/                        # Security components
│   │   │   │   ├── CustomOAuth2UserService.java
│   │   │   │   ├── UserPrincipal.java
│   │   │   │   └── PermissionEvaluator.java
│   │   │   │
│   │   │   └── exception/                       # Exception handling
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── PaymentException.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                  # Main configuration
│   │       ├── application-dev.yml              # Development profile
│   │       ├── application-prod.yml             # Production profile
│   │       │
│   │       ├── templates/                       # Thymeleaf templates
│   │       │   ├── layout/
│   │       │   │   ├── base.html               # Base layout
│   │       │   │   ├── header.html             # Header fragment
│   │       │   │   └── footer.html             # Footer fragment
│   │       │   │
│   │       │   ├── home.html                   # Home page
│   │       │   ├── food/
│   │       │   │   ├── list.html               # Catalog listing
│   │       │   │   └── detail.html             # Item details
│   │       │   │
│   │       │   ├── cart/
│   │       │   │   └── view.html               # Shopping cart
│   │       │   │
│   │       │   ├── order/
│   │       │   │   ├── checkout.html           # Checkout page
│   │       │   │   ├── confirmation.html       # Order confirmed
│   │       │   │   └── history.html            # Order history
│   │       │   │
│   │       │   ├── rating/
│   │       │   │   └── form.html               # Rating submission
│   │       │   │
│   │       │   ├── admin/
│   │       │   │   ├── dashboard.html          # Admin dashboard
│   │       │   │   ├── users.html              # User management
│   │       │   │   ├── categories.html         # Category management
│   │       │   │   ├── orders.html             # Order management
│   │       │   │   └── statistics.html         # Statistics
│   │       │   │
│   │       │   └── error/
│   │       │       ├── 404.html
│   │       │       └── 500.html
│   │       │
│   │       ├── static/                         # Static assets
│   │       │   ├── css/
│   │       │   │   └── custom.css             # Custom Bootstrap overrides
│   │       │   ├── js/
│   │       │   │   ├── cart.js                # Cart interactions
│   │       │   │   ├── rating.js              # Rating widget
│   │       │   │   └── share.js               # Social sharing
│   │       │   └── images/
│   │       │       └── logo.png
│   │       │
│   │       ├── db/migration/                   # Flyway migrations
│   │       │   ├── V1__create_users_table.sql
│   │       │   ├── V2__create_categories_table.sql
│   │       │   ├── V3__create_food_items_table.sql
│   │       │   ├── V4__create_orders_table.sql
│   │       │   ├── V5__create_ratings_table.sql
│   │       │   └── V6__create_carts_table.sql
│   │       │
│   │       └── email-templates/                # Email templates
│   │           ├── order-confirmation.html
│   │           └── order-status-update.html
│   │
│   └── test/
│       └── java/com/foodshop/
│           ├── integration/                     # Integration tests (Testcontainers)
│           │   ├── OrderFlowIntegrationTest.java
│           │   ├── AuthenticationIntegrationTest.java
│           │   └── FileStorageIntegrationTest.java
│           │
│           ├── service/                         # Unit tests
│           │   ├── OrderServiceTest.java
│           │   ├── CartServiceTest.java
│           │   └── RatingServiceTest.java
│           │
│           ├── controller/                      # Controller tests (MockMvc)
│           │   ├── FoodItemControllerTest.java
│           │   └── CartControllerTest.java
│           │
│           └── e2e/                            # End-to-end tests (Selenium)
│               ├── OrderJourneyTest.java
│               └── RatingJourneyTest.java
│
└── docker/                                     # Docker configurations
    ├── postgres/
    │   └── init.sql                           # Database initialization
    ├── redis/
    │   └── redis.conf                         # Redis configuration
    └── minio/
        └── policies/                          # MinIO bucket policies
            └── public-read.json
```

**Structure Decision**: Monolithic Spring Boot web application with MVC architecture. Server-side rendering via Thymeleaf keeps frontend simple while Bootstrap 5 ensures mobile responsiveness. Package structure follows domain-driven design with clear separation: domain entities, repositories (data access), services (business logic), controllers (HTTP handlers), and DTOs (API contracts). Integration with external services (OAuth2, Stripe, MinIO, Redis) isolated in dedicated config and service classes for testability.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
