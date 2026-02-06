# Tasks: Food Shop Application

**Input**: Design documents from `/specs/001-food-shop-app/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅
**Constitution**: AC-Food-Shop Constitution v1.0.0 - TDD mandatory, 80% coverage required

**Tests**: All test tasks are REQUIRED per Constitution Principle II (Testing Standards). TDD workflow: Write test → Red → Green → Refactor.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing. Each user story is a complete, independently testable increment.

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3...)
- **No story label**: Setup or foundational phase tasks

---

## Phase 1: Setup (Project Initialization) ✅ COMPLETE

**Purpose**: Create project structure and install base dependencies

- [X] T001 Create Maven project structure with standard directories (src/main/java, src/main/resources, src/test/java)
- [X] T002 Initialize pom.xml with Spring Boot 3.2.x parent, Java 21 compiler config, and dependency management
- [X] T003 [P] Add Spring Boot starters to pom.xml: spring-boot-starter-web, spring-boot-starter-thymeleaf, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-oauth2-client, spring-boot-starter-mail, spring-boot-starter-validation
- [X] T004 [P] Add PostgreSQL driver, Redis dependencies (spring-session-data-redis), and MinIO client to pom.xml
- [X] T005 [P] Add Stripe Java SDK dependency to pom.xml
- [X] T006 [P] Add testing dependencies to pom.xml: junit-jupiter, spring-boot-starter-test, testcontainers (postgresql, redis, localstack), mockito, selenium
- [X] T007 [P] Add code quality dependencies to pom.xml: checkstyle-maven-plugin, spotbugs-maven-plugin, jacoco-maven-plugin
- [X] T008 Create checkstyle.xml with Google Java Style Guide configuration
- [X] T009 [P] Create .gitignore for Java/Maven projects (target/, .idea/, *.iml, .env)
- [X] T010 [P] Create .env.example with all required environment variables (DB, Redis, MinIO, OAuth2, Stripe, Mail)
- [X] T011 Create docker-compose.yml with services: postgres (16), redis (7), minio, app
- [X] T012 [P] Create Dockerfile for Spring Boot application with Java 21 base image
- [X] T013 Create application.yml with profiles: default, dev, prod
- [X] T014 [P] Create application-dev.yml with local development settings
- [X] T015 [P] Create application-prod.yml with production settings (externalized secrets)

---

## Phase 2: Foundational (Blocking Prerequisites for ALL User Stories) ✅ COMPLETE

**Purpose**: Core infrastructure that MUST be complete before ANY user story implementation can begin

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database Foundation

- [X] T016 Create Flyway migration V001__create_users_table.sql with User entity schema (id, email, name, avatar_url, provider, external_id, role, status, created_at, updated_at)
- [X] T017 Create Flyway migration V002__create_categories_table.sql with Category entity schema (id, name, description, image_url, display_order, active, created_at)
- [X] T018 Create Flyway migration V003__create_food_items_table.sql with FoodItem entity schema (id, name, description, price, image_url, thumbnail_url, category_id, available, avg_rating, rating_count, created_at)
- [X] T019 Create Flyway migration V004__create_carts_and_cart_items_tables.sql with Cart (id, user_id, created_at, updated_at) and CartItem (id, cart_id, food_item_id, quantity, added_at)
- [X] T020 Create Flyway migration V005__create_orders_and_order_items_tables.sql with Order (id, user_id, status, total_amount, stripe_session_id, created_at, updated_at) and OrderItem (id, order_id, food_item_id, quantity, price)
- [X] T021 Create Flyway migration V006__create_ratings_table.sql with Rating entity schema (id, user_id, food_item_id, stars, review_text, created_at, verified_purchase)

### Domain Entities (JPA)

- [X] T022 [P] Create User entity in src/main/java/com/foodshop/domain/User.java with JPA annotations and relationships
- [X] T023 [P] Create Category entity in src/main/java/com/foodshop/domain/Category.java with JPA annotations
- [X] T024 [P] Create FoodItem entity in src/main/java/com/foodshop/domain/FoodItem.java with JPA annotations and category relationship
- [X] T025 [P] Create Cart entity in src/main/java/com/foodshop/domain/Cart.java with JPA annotations
- [X] T026 [P] Create CartItem entity in src/main/java/com/foodshop/domain/CartItem.java with JPA annotations
- [X] T027 [P] Create Order entity in src/main/java/com/foodshop/domain/Order.java with JPA annotations and OrderStatus enum
- [X] T028 [P] Create OrderItem entity in src/main/java/com/foodshop/domain/OrderItem.java with JPA annotations
- [X] T029 [P] Create Rating entity in src/main/java/com/foodshop/domain/Rating.java with JPA annotations

### Repositories (Spring Data JPA)

- [X] T030 [P] Create UserRepository interface in src/main/java/com/foodshop/repository/UserRepository.java extending JpaRepository with custom queries (findByEmail, findByProviderAndExternalId)
- [X] T031 [P] Create CategoryRepository interface in src/main/java/com/foodshop/repository/CategoryRepository.java with custom queries (findByActiveOrderByDisplayOrder)
- [X] T032 [P] Create FoodItemRepository interface in src/main/java/com/foodshop/repository/FoodItemRepository.java with custom queries (findByCategoryId, findByAvailable, search methods with @EntityGraph to prevent N+1)
- [X] T033 [P] Create CartRepository interface in src/main/java/com/foodshop/repository/CartRepository.java with findByUserId
- [X] T034 [P] Create OrderRepository interface in src/main/java/com/foodshop/repository/OrderRepository.java with custom queries (findByUserId, findByStatus, findByUserIdAndStatus with @EntityGraph)
- [X] T035 [P] Create RatingRepository interface in src/main/java/com/foodshop/repository/RatingRepository.java with custom queries (findByFoodItemId, findByUserIdAndFoodItemId, calculateAvgRating)

### Security & Authentication

- [X] T036 Create SecurityConfig in src/main/java/com/foodshop/config/SecurityConfig.java with OAuth2 login, session management, CSRF protection, and role-based authorization
- [X] T037 Create CustomOAuth2UserService in src/main/java/com/foodshop/security/CustomOAuth2UserService.java to handle OAuth2 user info and create/update User entities
- [X] T038 Create UserPrincipal in src/main/java/com/foodshop/security/UserPrincipal.java implementing UserDetails for Spring Security
- [X] T039 Create AuthController in src/main/java/com/foodshop/controller/AuthController.java with OAuth2 success/failure handlers

### Configuration

- [X] T040 [P] Create RedisConfig in src/main/java/com/foodshop/config/RedisConfig.java with Spring Session configuration (30min timeout)
- [X] T041 [P] Create MinIOConfig in src/main/java/com/foodshop/config/MinIOConfig.java with client initialization and bucket creation
- [X] T042 [P] Create StripeConfig in src/main/java/com/foodshop/config/StripeConfig.java with API key configuration
- [X] T043 [P] Create MailConfig in src/main/java/com/foodshop/config/MailConfig.java with SMTP settings and async executor
- [X] T044 [P] Create WebMvcConfig in src/main/java/com/foodshop/config/WebMvcConfig.java with resource handlers for static assets

### Core Services

- [X] T045 [P] Create FileStorageService in src/main/java/com/foodshop/service/FileStorageService.java with MinIO upload, download, delete methods and image resizing
- [X] T046 [P] Create EmailService in src/main/java/com/foodshop/service/EmailService.java with @Async methods for order notifications using Thymeleaf email templates

### Exception Handling

- [X] T047 Create custom exceptions in src/main/java/com/foodshop/exception/: ResourceNotFoundException, PaymentException, UnauthorizedException
- [X] T048 Create GlobalExceptionHandler in src/main/java/com/foodshop/exception/GlobalExceptionHandler.java with @ControllerAdvice for centralized error handling

### Thymeleaf Base Templates

- [X] T049 [P] Create base layout template in src/main/resources/templates/layout/base.html with Bootstrap 5, navigation, footer
- [X] T050 [P] Create Thymeleaf fragments in src/main/resources/templates/fragments/: header.html, footer.html, navigation.html
- [X] T051 [P] Create error page templates in src/main/resources/templates/error/: 404.html, 500.html, 403.html

### Testing Infrastructure

- [X] T052 [P] Create TestcontainersConfig in src/test/java/com/foodshop/config/TestcontainersConfig.java with PostgreSQL, Redis, LocalStack containers
- [X] T053 [P] Create BaseIntegrationTest abstract class in src/test/java/com/foodshop/BaseIntegrationTest.java with @SpringBootTest and Testcontainers annotations
- [X] T054 [P] Create TestDataBuilder utility in src/test/java/com/foodshop/util/TestDataBuilder.java for creating test entities

**Checkpoint**: ✅ Foundation complete - User story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Browse and Order Food Items (Priority: P1) 🎯 MVP

**Goal**: Enable customers to browse food catalog, add items to cart, checkout, and place orders with email notifications. This is the core revenue-generating feature and represents the minimum viable product.

**Independent Test**: Create a test user via OAuth2, browse catalog, add items to cart, complete checkout with Stripe test card, verify order status = CONFIRMED and email notifications sent to both customer and admin.

### Tests for User Story 1 (TDD - Write FIRST) ✅

- [X] T055 [P] [US1] Unit test for FoodItemService.findAll() in src/test/java/com/foodshop/service/FoodItemServiceTest.java (verify pagination, filtering)
- [X] T056 [P] [US1] Unit test for FoodItemService.findById() in src/test/java/com/foodshop/service/FoodItemServiceTest.java (verify entity graph loading)
- [X] T057 [P] [US1] Unit test for CartService.addItem() in src/test/java/com/foodshop/service/CartServiceTest.java (verify cart creation, quantity updates)
- [X] T058 [P] [US1] Unit test for CartService.updateItemQuantity() in src/test/java/com/foodshop/service/CartServiceTest.java
- [X] T059 [P] [US1] Unit test for CartService.removeItem() in src/test/java/com/foodshop/service/CartServiceTest.java
- [X] T060 [P] [US1] Unit test for OrderService.createOrder() in src/test/java/com/foodshop/service/OrderServiceTest.java (verify order creation, cart clearing, Stripe session creation)
- [X] T061 [P] [US1] Unit test for PaymentService.createCheckoutSession() in src/test/java/com/foodshop/service/PaymentServiceTest.java (mock Stripe API)
- [X] T062 [P] [US1] Unit test for PaymentService.handleWebhook() in src/test/java/com/foodshop/service/PaymentServiceTest.java (verify order status update, email trigger)
- [X] T063 [P] [US1] Integration test for cart operations in src/test/java/com/foodshop/integration/CartIntegrationTest.java (test with Testcontainers PostgreSQL + Redis)
- [X] T064 [P] [US1] Integration test for order creation flow in src/test/java/com/foodshop/integration/OrderIntegrationTest.java (end-to-end with mock Stripe)
- [X] T065 [P] [US1] Contract test for /api/v1/cart/add endpoint in src/test/java/com/foodshop/controller/CartControllerTest.java (MockMvc)
- [X] T066 [P] [US1] Contract test for /api/v1/orders endpoint in src/test/java/com/foodshop/controller/OrderControllerTest.java (MockMvc)
- [X] T067 [US1] E2E test for browse-to-order journey in src/test/java/com/foodshop/e2e/BrowseAndOrderE2ETest.java (Selenium: browse catalog → add to cart → checkout → verify order confirmation page)

### Implementation for User Story 1

**Category & Food Item Browsing**

- [X] T068 [P] [US1] Implement CategoryService in src/main/java/com/foodshop/service/CategoryService.java (findAll, findById methods)
- [X] T069 [P] [US1] Implement FoodItemService in src/main/java/com/foodshop/service/FoodItemService.java (findAll with pagination, findById, findByCategoryId, search methods)
- [X] T070 [US1] Create HomeController in src/main/java/com/foodshop/controller/HomeController.java with index() method rendering home page with featured items
- [X] T071 [US1] Create FoodItemController in src/main/java/com/foodshop/controller/FoodItemController.java with browse(), detail() methods
- [X] T072 [P] [US1] Create home page template in src/main/resources/templates/index.html with hero section and featured food items grid
- [X] T073 [P] [US1] Create food catalog template in src/main/resources/templates/food/list.html with category filter, search, pagination
- [X] T074 [P] [US1] Create food detail template in src/main/resources/templates/food/detail.html with images, description, ratings, add-to-cart button

**Shopping Cart**

- [X] T075 [P] [US1] Implement CartService in src/main/java/com/foodshop/service/CartService.java (getOrCreateCart, addItem, updateItemQuantity, removeItem, clearCart)
- [X] T076 [US1] Create CartController in src/main/java/com/foodshop/controller/CartController.java with viewCart() and AJAX endpoints (addItem, updateItem, removeItem)
- [X] T077 [P] [US1] Create cart page template in src/main/resources/templates/cart/view.html with items list, quantities, subtotal, checkout button
- [X] T078 [P] [US1] Create cart JavaScript in src/main/resources/static/js/cart.js for AJAX cart operations with loading states

**Order & Payment**

- [X] T079 [P] [US1] Implement PaymentService in src/main/java/com/foodshop/service/PaymentService.java (createCheckoutSession, handleWebhook methods using Stripe SDK)
- [X] T080 [US1] Implement OrderService in src/main/java/com/foodshop/service/OrderService.java (createOrder, updateStatus, findByUser methods)
- [X] T081 [US1] Create OrderController in src/main/java/com/foodshop/controller/OrderController.java (createOrder, orderHistory, orderDetail, success/cancel handlers)
- [X] T082 [US1] Create Stripe webhook controller in src/main/java/com/foodshop/controller/StripeWebhookController.java (handle checkout.session.completed event)
- [X] T083 [P] [US1] Create checkout page template in src/main/resources/templates/checkout/index.html with order summary and Stripe checkout button
- [X] T084 [P] [US1] Create order confirmation template in src/main/resources/templates/orders/success.html
- [X] T085 [P] [US1] Create order history template in src/main/resources/templates/orders/list.html with order cards showing status, items, total
- [X] T086 [P] [US1] Create order detail template in src/main/resources/templates/orders/detail.html with full order information

**Email Notifications**

- [X] T087 [P] [US1] Create email templates in src/main/resources/templates/email/: order-confirmation-customer.html, order-notification-admin.html
- [X] T088 [US1] Implement order notification logic in EmailService.sendOrderNotifications() method (async, called from OrderService after order creation)

**Seed Data for Testing**

- [X] T089 [US1] Create seed data SQL script in src/main/resources/db/seed/V900__seed_initial_data.sql (categories, sample food items with images)

**Documentation**

- [X] T090 [US1] Update quickstart.md with User Story 1 setup instructions (OAuth2 config, Stripe test mode, email testing)

---

## Phase 4: User Story 2 - Rate and Share Food Items (Priority: P2)

**Goal**: Enable customers to rate food items they've ordered and share items via social media or direct links. Enhances engagement and drives organic growth.

**Independent Test**: Create test user with completed orders, submit ratings (1-5 stars with review text), verify avg_rating updates on FoodItem entity, generate share links, verify social media share URLs are correct.

### Tests for User Story 2 (TDD - Write FIRST) ✅

- [ ] T091 [P] [US2] Unit test for RatingService.submitRating() in src/test/java/com/foodshop/service/RatingServiceTest.java (verify rating creation, avg_rating recalculation, duplicate prevention)
- [ ] T092 [P] [US2] Unit test for RatingService.canUserRate() in src/test/java/com/foodshop/service/RatingServiceTest.java (verify user has purchased item)
- [ ] T093 [P] [US2] Unit test for ShareService.generateShareUrl() in src/test/java/com/foodshop/service/ShareServiceTest.java
- [ ] T094 [P] [US2] Unit test for ShareService.getSocialShareLinks() in src/test/java/com/foodshop/service/ShareServiceTest.java
- [ ] T095 [P] [US2] Integration test for rating submission flow in src/test/java/com/foodshop/integration/RatingIntegrationTest.java (verify database updates, avg_rating calculation)
- [ ] T096 [P] [US2] Contract test for /api/v1/ratings endpoint in src/test/java/com/foodshop/controller/RatingControllerTest.java (MockMvc)
- [ ] T097 [US2] E2E test for rating submission in src/test/java/com/foodshop/e2e/RatingE2ETest.java (Selenium: navigate to order history → rate item → verify rating appears)

### Implementation for User Story 2

**Rating System**

- [ ] T098 [P] [US2] Implement RatingService in src/main/java/com/foodshop/service/RatingService.java (submitRating, updateRating, canUserRate, findByFoodItem, recalculateAvgRating methods)
- [ ] T099 [US2] Create RatingController in src/main/java/com/foodshop/controller/RatingController.java with submitRating(), updateRating() AJAX endpoints
- [ ] T100 [P] [US2] Add rating section to food detail template in src/main/resources/templates/food/detail.html (rating breakdown bars, review list with pagination)
- [ ] T101 [P] [US2] Create rating modal component in src/main/resources/templates/fragments/rating-modal.html (star picker, review textarea)
- [ ] T102 [P] [US2] Create rating JavaScript in src/main/resources/static/js/rating.js for interactive star picker and AJAX submission
- [ ] T103 [US2] Add "Rate Items" action to order history page in src/main/resources/templates/orders/list.html (only for delivered orders)

**Social Sharing**

- [ ] T104 [P] [US2] Implement ShareService in src/main/java/com/foodshop/service/ShareService.java (generateShareUrl with UTM params, getSocialShareLinks for Facebook/Twitter/WhatsApp)
- [ ] T105 [US2] Create ShareController in src/main/java/com/foodshop/controller/ShareController.java with getShareData() AJAX endpoint
- [ ] T106 [P] [US2] Add share button to food detail page in src/main/resources/templates/food/detail.html with social media icons
- [ ] T107 [P] [US2] Create share modal component in src/main/resources/templates/fragments/share-modal.html (social buttons, copy link)
- [ ] T108 [P] [US2] Create share JavaScript in src/main/resources/static/js/share.js for modal interactions and clipboard copy

---

## Phase 5: User Story 3 - Admin User Management (Priority: P3)

**Goal**: Enable admins to view, search, filter, and manage user accounts. Essential for platform governance and long-term health.

**Independent Test**: Authenticate as admin, access /admin/users, search/filter users by criteria, view user details with order history, deactivate a user account, verify user cannot login.

### Tests for User Story 3 (TDD - Write FIRST) ✅

- [ ] T109 [P] [US3] Unit test for UserService.findAll() with pagination in src/test/java/com/foodshop/service/UserServiceTest.java
- [ ] T110 [P] [US3] Unit test for UserService.search() with filters in src/test/java/com/foodshop/service/UserServiceTest.java
- [ ] T111 [P] [US3] Unit test for UserService.updateStatus() in src/test/java/com/foodshop/service/UserServiceTest.java
- [ ] T112 [P] [US3] Integration test for user management in src/test/java/com/foodshop/integration/AdminUserIntegrationTest.java (verify search, status updates persist)
- [ ] T113 [P] [US3] Contract test for /api/v1/admin/users endpoints in src/test/java/com/foodshop/controller/AdminUserControllerTest.java (MockMvc, verify @PreAuthorize ADMIN)
- [ ] T114 [US3] E2E test for admin user management in src/test/java/com/foodshop/e2e/AdminUserE2ETest.java (Selenium: login as admin → search users → update status)

### Implementation for User Story 3

- [ ] T115 [P] [US3] Implement UserService in src/main/java/com/foodshop/service/UserService.java (findAll with pagination, search with filters, findById, updateStatus methods)
- [ ] T116 [US3] Create AdminUserController in src/main/java/com/foodshop/controller/admin/AdminUserController.java (@PreAuthorize("ADMIN")) with listUsers(), userDetail(), updateUserStatus() methods
- [ ] T117 [P] [US3] Create admin dashboard template in src/main/resources/templates/admin/dashboard.html with key metrics and navigation
- [ ] T118 [P] [US3] Create admin user list template in src/main/resources/templates/admin/users/list.html with table, search form, filters (status, join date), pagination
- [ ] T119 [P] [US3] Create admin user detail template in src/main/resources/templates/admin/users/detail.html with user info, order history, status management
- [ ] T120 [P] [US3] Create admin navigation fragment in src/main/resources/templates/fragments/admin-nav.html for consistent admin panel navigation

---

## Phase 6: User Story 4 - Admin Food Category Management (Priority: P4)

**Goal**: Enable admins to create, edit, organize, and delete food categories for better catalog structure.

**Independent Test**: Authenticate as admin, create new category with name/description/image, reorder categories via drag-and-drop or priority field, edit category details, verify changes appear in customer-facing catalog.

### Tests for User Story 4 (TDD - Write FIRST) ✅

- [ ] T121 [P] [US4] Unit test for CategoryService.create() in src/test/java/com/foodshop/service/CategoryServiceTest.java
- [ ] T122 [P] [US4] Unit test for CategoryService.update() in src/test/java/com/foodshop/service/CategoryServiceTest.java
- [ ] T123 [P] [US4] Unit test for CategoryService.delete() in src/test/java/com/foodshop/service/CategoryServiceTest.java (verify soft delete or item reassignment)
- [ ] T124 [P] [US4] Unit test for CategoryService.reorder() in src/test/java/com/foodshop/service/CategoryServiceTest.java
- [ ] T125 [P] [US4] Integration test for category management in src/test/java/com/foodshop/integration/AdminCategoryIntegrationTest.java (test with Testcontainers + MinIO)
- [ ] T126 [P] [US4] Contract test for /api/v1/admin/categories endpoints in src/test/java/com/foodshop/controller/AdminCategoryControllerTest.java (MockMvc)
- [ ] T127 [US4] E2E test for category management in src/test/java/com/foodshop/e2e/AdminCategoryE2ETest.java (Selenium: create category → upload image → reorder → verify)

### Implementation for User Story 4

- [ ] T128 [US4] Add create(), update(), delete(), reorder() methods to CategoryService in src/main/java/com/foodshop/service/CategoryService.java
- [ ] T129 [US4] Create AdminCategoryController in src/main/java/com/foodshop/controller/admin/AdminCategoryController.java with CRUD and reorder endpoints
- [ ] T130 [P] [US4] Create admin category list template in src/main/resources/templates/admin/categories/list.html with drag-and-drop reordering
- [ ] T131 [P] [US4] Create admin category form template in src/main/resources/templates/admin/categories/form.html (create/edit with image upload)
- [ ] T132 [P] [US4] Create category management JavaScript in src/main/resources/static/js/admin/category-manager.js (drag-and-drop, image preview, AJAX CRUD)

---

## Phase 7: User Story 5 - Admin Order Management (Priority: P5)

**Goal**: Enable admins to view all orders, filter by status/date/customer, update order status, view order details. Essential for operations management.

**Independent Test**: Authenticate as admin, access /admin/orders, filter orders by status (PENDING, CONFIRMED, PREPARING, DELIVERED), update an order status, verify customer receives email notification.

### Tests for User Story 5 (TDD - Write FIRST) ✅

- [ ] T133 [P] [US5] Unit test for OrderService.findAll() with filters in src/test/java/com/foodshop/service/OrderServiceTest.java
- [ ] T134 [P] [US5] Unit test for OrderService.updateStatus() in src/test/java/com/foodshop/service/OrderServiceTest.java (verify email trigger)
- [ ] T135 [P] [US5] Integration test for order status updates in src/test/java/com/foodshop/integration/AdminOrderIntegrationTest.java (verify status workflow, email sending)
- [ ] T136 [P] [US5] Contract test for /api/v1/admin/orders endpoints in src/test/java/com/foodshop/controller/AdminOrderControllerTest.java (MockMvc)
- [ ] T137 [US5] E2E test for admin order management in src/test/java/com/foodshop/e2e/AdminOrderE2ETest.java (Selenium: filter orders → update status → verify)

### Implementation for User Story 5

- [ ] T138 [US5] Add findAll() with filters (status, dateRange, customerId), updateStatus() methods to OrderService in src/main/java/com/foodshop/service/OrderService.java
- [ ] T139 [US5] Create AdminOrderController in src/main/java/com/foodshop/controller/admin/AdminOrderController.java with listOrders(), orderDetail(), updateOrderStatus() methods
- [ ] T140 [P] [US5] Create admin order list template in src/main/resources/templates/admin/orders/list.html with filters, status badges, pagination
- [ ] T141 [P] [US5] Create admin order detail template in src/main/resources/templates/admin/orders/detail.html with customer info, items, status workflow
- [ ] T142 [P] [US5] Create email template for order status updates in src/main/resources/templates/email/order-status-update.html
- [ ] T143 [US5] Add order status update notification logic to EmailService in src/main/java/com/foodshop/service/EmailService.java

---

## Phase 8: User Story 6 - Admin Food Item Statistics (Priority: P6)

**Goal**: Enable admins to view analytics on food items: most popular, revenue per item, ratings, trends. Supports data-driven decision making.

**Independent Test**: Seed system with historical order and rating data, authenticate as admin, access /admin/statistics, view item-specific stats (total orders, revenue, avg rating), filter by date range, export CSV.

### Tests for User Story 6 (TDD - Write FIRST) ✅

- [ ] T144 [P] [US6] Unit test for StatisticsService.getFoodItemStats() in src/test/java/com/foodshop/service/StatisticsServiceTest.java (verify revenue calculation, aggregations)
- [ ] T145 [P] [US6] Unit test for StatisticsService.getTopItems() in src/test/java/com/foodshop/service/StatisticsServiceTest.java (verify sorting, limits)
- [ ] T146 [P] [US6] Unit test for StatisticsService.getTrendingItems() in src/test/java/com/foodshop/service/StatisticsServiceTest.java (verify date range filtering)
- [ ] T147 [P] [US6] Integration test for statistics generation in src/test/java/com/foodshop/integration/AdminStatisticsIntegrationTest.java (verify complex queries, performance)
- [ ] T148 [P] [US6] Contract test for /api/v1/admin/statistics endpoints in src/test/java/com/foodshop/controller/AdminStatisticsControllerTest.java (MockMvc)

### Implementation for User Story 6

- [ ] T149 [P] [US6] Create StatisticsDto classes in src/main/java/com/foodshop/dto/: FoodItemStatsDto, DashboardStatsDto, TrendDto
- [ ] T150 [US6] Implement StatisticsService in src/main/java/com/foodshop/service/StatisticsService.java (getFoodItemStats, getTopItems, getTrendingItems, calculateRevenue methods with custom JPQL queries)
- [ ] T151 [US6] Create AdminStatisticsController in src/main/java/com/foodshop/controller/admin/AdminStatisticsController.java with dashboard(), foodItemStats(), exportCsv() methods
- [ ] T152 [P] [US6] Create admin statistics dashboard template in src/main/resources/templates/admin/statistics/dashboard.html with key metrics cards and charts
- [ ] T153 [P] [US6] Create admin food item stats template in src/main/resources/templates/admin/statistics/food-items.html with sortable table, filters, charts
- [ ] T154 [P] [US6] Add Chart.js library to project for statistics visualizations
- [ ] T155 [P] [US6] Create statistics JavaScript in src/main/resources/static/js/admin/statistics.js for chart rendering and data refresh
- [ ] T156 [US6] Implement CSV export utility in src/main/java/com/foodshop/util/CsvExporter.java for statistics data

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Final refinements, performance optimization, accessibility, and production readiness

### Performance Optimization (Constitution Principle IV)

- [ ] T157 [P] Add Redis caching to CategoryService.findAll() with @Cacheable annotation (30min TTL)
- [ ] T158 [P] Add Redis caching to FoodItemService.findByCategoryId() with @Cacheable annotation
- [ ] T159 [P] Configure HikariCP connection pool settings in application.yml (max pool size 20, connection timeout 30s)
- [ ] T160 [P] Add database query monitoring with Micrometer in src/main/java/com/foodshop/config/MetricsConfig.java
- [ ] T161 Implement image optimization in FileStorageService (generate thumbnails 200x200, medium 800x800 for food items)
- [ ] T162 [P] Add HTTP caching headers for static assets in WebMvcConfig (ETags, Cache-Control max-age=31536000)
- [ ] T163 Configure Spring Boot Actuator metrics and health endpoints in application.yml

### Accessibility (Constitution Principle III - WCAG 2.1 AA)

- [ ] T164 [P] Add ARIA labels to all interactive elements in templates (cart buttons, rating stars, form inputs)
- [ ] T165 [P] Add keyboard navigation support to rating star picker in src/main/resources/static/js/rating.js (arrow keys, Enter)
- [ ] T166 [P] Add keyboard navigation support to cart quantity controls in src/main/resources/static/js/cart.js
- [ ] T167 [P] Verify color contrast ratios in custom CSS (use contrast checker tool)
- [ ] T168 [P] Add skip-to-content links in base layout template
- [ ] T169 Run Lighthouse accessibility audit and fix identified issues

### Code Quality (Constitution Principle I)

- [ ] T170 Run Checkstyle across entire codebase and fix violations
- [ ] T171 Run SpotBugs across entire codebase and fix high-priority issues
- [ ] T172 Add JavaDoc comments to all public service methods
- [ ] T173 Refactor any methods >50 lines to improve readability (extract helper methods)
- [ ] T174 Run dependency vulnerability scan (mvn dependency-check:check) and update vulnerable dependencies

### Testing Coverage (Constitution Principle II - 80% minimum)

- [ ] T175 Run JaCoCo coverage report (mvn verify jacoco:report) and verify >80% line coverage
- [ ] T176 Add missing unit tests for service methods below 80% coverage
- [ ] T177 Add missing integration tests for uncovered repository queries
- [ ] T178 Run mutation testing (PIT) to verify test quality

### Documentation

- [ ] T179 Update README.md with project overview, features, tech stack, setup instructions
- [ ] T180 Document all API endpoints in contracts/README.md with examples
- [ ] T181 Update quickstart.md with complete developer onboarding guide
- [ ] T182 Create CONTRIBUTING.md with code style guide, PR process, constitution checklist
- [ ] T183 Create deployment guide in docs/DEPLOYMENT.md (Docker Compose, environment variables, database migrations)

### Production Readiness

- [ ] T184 Configure structured logging with SLF4J + Logback (JSON format for production)
- [ ] T185 Add security headers to responses in SecurityConfig (X-Content-Type-Options, X-Frame-Options, HSTS)
- [ ] T186 Configure CSRF token handling for AJAX requests
- [ ] T187 Set up error tracking with Sentry or similar (optional, for production)
- [ ] T188 Create GitHub Actions CI/CD pipeline (.github/workflows/ci.yml) with build, test, checkstyle, coverage checks
- [ ] T189 Create Docker Compose production file (docker-compose.prod.yml) with proper environment variable handling
- [ ] T190 Create database backup/restore scripts in scripts/backup.sh and scripts/restore.sh

---

## Dependencies & Execution Strategy

### Dependency Graph (User Story Completion Order)

```
Phase 1 (Setup) + Phase 2 (Foundation)
  ↓
Phase 3: US1 (MVP - Browse & Order) ← MUST COMPLETE FIRST
  ↓
Phase 4: US2 (Rate & Share) ← Depends on US1 (needs completed orders to rate)
  ↓
Phase 5-8: US3, US4, US5, US6 (Admin Features) ← Can be done in parallel after US1
  ↓
Phase 9: Polish & Cross-Cutting Concerns
```

### Parallel Execution Opportunities

**Within Phase 2 (Foundation)**:
- Database migrations (T016-T021) can run sequentially but be prepared in parallel
- Entity creation (T022-T029) can be done in parallel
- Repository creation (T030-T035) can be done in parallel
- Configuration classes (T040-T044) can be done in parallel
- Thymeleaf templates (T049-T051) can be done in parallel
- Test infrastructure (T052-T054) can be done in parallel

**Within Phase 3 (US1 - MVP)**:
- Tests (T055-T067) should be written in parallel BEFORE implementation
- Service layer (T068, T069, T075, T079, T080) can be developed in parallel once tests are written
- Controllers (T070, T071, T076, T081, T082) can be developed in parallel after services
- Templates (T072-T074, T077, T083-T086) can be developed in parallel
- JavaScript (T078) and email templates (T087) can be done in parallel

**Admin Features (Phase 5-8)**:
- US3 (User Management), US4 (Category Management), US5 (Order Management), US6 (Statistics) can all be implemented in parallel after US1 is complete
- Each user story has independent tests, services, controllers, and templates

**Phase 9 (Polish)**:
- Performance optimization (T157-T163) can be done in parallel
- Accessibility fixes (T164-T169) can be done in parallel
- Documentation (T179-T183) can be done in parallel

### Suggested MVP Scope

**Minimum viable product**: Phase 1 + Phase 2 + Phase 3 (User Story 1 only)

This delivers:
- Social authentication (Google, Facebook)
- Browse food catalog
- Shopping cart
- Order placement with Stripe payment
- Email notifications

Estimated: ~150 tasks (including tests) for MVP

**Incremental delivery plan**:
1. **Sprint 1** (MVP): Phase 1, Phase 2, Phase 3 (US1) - 2-3 weeks
2. **Sprint 2** (Engagement): Phase 4 (US2) - 1 week
3. **Sprint 3** (Admin - Core): Phase 5 (US3), Phase 6 (US4) - 1-2 weeks
4. **Sprint 4** (Admin - Advanced): Phase 7 (US5), Phase 8 (US6) - 1-2 weeks
5. **Sprint 5** (Polish): Phase 9 - 1 week

Total estimated: 6-9 weeks for full feature completion

---

## Task Summary

- **Total Tasks**: 190
- **Setup & Foundation**: 54 tasks (T001-T054)
- **User Story 1 (MVP)**: 36 tasks (T055-T090) - includes 13 test tasks
- **User Story 2**: 18 tasks (T091-T108) - includes 7 test tasks
- **User Story 3**: 12 tasks (T109-T120) - includes 6 test tasks
- **User Story 4**: 12 tasks (T121-T132) - includes 7 test tasks
- **User Story 5**: 11 tasks (T133-T143) - includes 5 test tasks
- **User Story 6**: 13 tasks (T144-T156) - includes 5 test tasks
- **Polish & Production**: 34 tasks (T157-T190)

**Parallelizable Tasks**: ~60% of tasks can run in parallel within each phase

**Test Tasks**: 43 test tasks total (23% of all tasks) - all REQUIRED per Constitution Principle II

**Estimated Effort**: 6-9 weeks with 2-3 developers working in parallel on user stories after foundation is complete
