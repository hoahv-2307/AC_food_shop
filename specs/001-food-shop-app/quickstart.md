# Quickstart Guide: Food Shop Application

**Feature**: Food Shop Application  
**Date**: 2026-02-06  
**Phase**: 1 - Development Setup & Testing Guide

## Overview

This quickstart guide helps developers set up the Food Shop Application locally, run tests, and understand common development workflows. The application uses Docker Compose for local development, ensuring consistent environments across the team.

## Prerequisites

Before starting, ensure you have:

- **Java 21 LTS** (OpenJDK or Oracle JDK)
- **Maven 3.9+** (or use Maven Wrapper included in project)
- **Docker Desktop** (Docker Engine + Docker Compose)
- **Git** (for cloning repository)
- **IDE**: IntelliJ IDEA (recommended) or VS Code with Java extensions

**Verify installations**:
```bash
java --version  # Should show "openjdk 21" or "java 21"
mvn --version   # Should show "Apache Maven 3.9.x"
docker --version && docker-compose --version
```

---

## Initial Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd food-shop
git checkout 001-food-shop-app
```

### 2. Configure Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` with your credentials:

```bash
# Database
POSTGRES_DB=foodshop
POSTGRES_USER=foodshop_user
POSTGRES_PASSWORD=change_me_in_production

# Redis
REDIS_PASSWORD=change_me_in_production

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin123
MINIO_BUCKET=food-images

# OAuth2 - Google (Get from https://console.cloud.google.com)
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret

# OAuth2 - Facebook (Get from https://developers.facebook.com)
FACEBOOK_APP_ID=your-facebook-app-id
FACEBOOK_APP_SECRET=your-facebook-app-secret

# Stripe (Get from https://dashboard.stripe.com/test/apikeys)
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Email (Optional for local development)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

**Note**: For local development, you can use Stripe test keys and skip email configuration (emails will be logged to console).

### 3. Start Infrastructure Services

Start PostgreSQL, Redis, and MinIO using Docker Compose:

```bash
docker-compose up -d postgres redis minio
```

**Verify services are running**:
```bash
docker-compose ps

# Expected output:
# NAME                  STATUS
# foodshop-postgres-1   Up 10 seconds   5432/tcp
# foodshop-redis-1      Up 10 seconds   6379/tcp
# foodshop-minio-1      Up 10 seconds   9000-9001/tcp
```

**Access MinIO Console** (optional):
- URL: http://localhost:9001
- Username: `minioadmin`
- Password: `minioadmin123`
- Create bucket: `food-images` with public-read policy

### 4. Run Database Migrations

Flyway migrations run automatically on application startup, but you can verify:

```bash
./mvnw flyway:info  # Show migration status
./mvnw flyway:migrate  # Run migrations manually
```

### 5. Build Application

```bash
./mvnw clean install
```

This command:
- Downloads dependencies
- Runs Checkstyle (code quality)
- Runs unit tests
- Runs integration tests (with Testcontainers)
- Packages application as JAR

**Expected output**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2:15 min
```

### 6. Run Application

**Option A: Using Maven** (development mode):
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Option B: Using Docker Compose** (production-like):
```bash
docker-compose up app
```

**Option C: Using JAR** (after build):
```bash
java -jar target/food-shop-1.0.0.jar --spring.profiles.active=dev
```

**Verify application started**:
```
INFO: Started FoodShopApplication in 12.345 seconds
```

### 7. Access Application

- **Home Page**: http://localhost:8080
- **Admin Dashboard**: http://localhost:8080/admin (requires ADMIN role)
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics

---

## Development Workflows

### Running Tests

**All Tests**:
```bash
./mvnw test
```

**Unit Tests Only**:
```bash
./mvnw test -Dtest=*Test
```

**Integration Tests** (with Testcontainers):
```bash
./mvnw test -Dtest=*IntegrationTest
```

**End-to-End Tests** (Selenium, requires Chrome):
```bash
./mvnw test -Dtest=*E2ETest
```

**Test Coverage Report** (JaCoCo):
```bash
./mvnw verify jacoco:report
open target/site/jacoco/index.html  # View coverage report
```

**Expected Coverage**: >80% line coverage (constitution requirement)

---

### Code Quality Checks

**Checkstyle** (Google Java Style Guide):
```bash
./mvnw checkstyle:check
```

**SpotBugs** (static analysis):
```bash
./mvnw spotbugs:check
```

**Fix common issues automatically**:
```bash
./mvnw spotless:apply  # Auto-format code
```

---

### Database Management

**Reset Database** (drop all tables and re-run migrations):
```bash
./mvnw flyway:clean flyway:migrate
```

**Seed Test Data**:
```bash
java -jar target/food-shop-1.0.0.jar --spring.profiles.active=dev --seed-data
```

Or use SQL script:
```bash
docker-compose exec postgres psql -U foodshop_user -d foodshop -f /docker-entrypoint-initdb.d/seed.sql
```

**Inspect Database**:
```bash
docker-compose exec postgres psql -U foodshop_user -d foodshop

# Example queries:
SELECT * FROM users LIMIT 10;
SELECT * FROM orders WHERE status = 'CONFIRMED';
SELECT f.name, f.avg_rating, f.rating_count FROM food_items f ORDER BY avg_rating DESC;
```

---

### OAuth2 Setup (Local Development)

#### Google OAuth2

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create new project: "Food Shop Dev"
3. Enable Google+ API
4. Create OAuth 2.0 credentials:
   - Application type: **Web application**
   - Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
5. Copy **Client ID** and **Client Secret** to `.env`

#### Facebook OAuth2

1. Go to [Facebook Developers](https://developers.facebook.com)
2. Create new app: "Food Shop Dev"
3. Add product: **Facebook Login**
4. Settings → Basic:
   - Copy **App ID** and **App Secret** to `.env`
5. Settings → Advanced → Valid OAuth Redirect URIs: `http://localhost:8080/login/oauth2/code/facebook`

---

### Stripe Payment Testing

#### Setup Test Mode

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/test/apikeys)
2. Copy **Publishable Key** and **Secret Key** to `.env`
3. Create webhook endpoint:
   - URL: `http://localhost:8080/api/v1/webhooks/stripe`
   - Events: `checkout.session.completed`, `payment_intent.payment_failed`
   - Copy **Webhook Secret** to `.env`

**Note**: For local testing, use [Stripe CLI](https://stripe.com/docs/stripe-cli) to forward webhooks:

```bash
stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe
```

#### Test Payment Flow

1. Add items to cart
2. Click "Checkout"
3. Redirected to Stripe Checkout
4. Use test card: `4242 4242 4242 4242` (any future expiry, any CVC)
5. Complete payment
6. Redirected back to order confirmation
7. Verify webhook received (check logs)
8. Verify order status: PENDING → CONFIRMED

**Test Card Numbers**:
- Success: `4242 4242 4242 4242`
- Decline: `4000 0000 0000 0002`
- Requires authentication: `4000 0025 0000 3155`

---

### Email Testing (Local)

For local development, emails are logged to console (not actually sent). To test real emails:

**Option A: Gmail SMTP** (recommended for dev):
1. Enable 2FA on your Gmail account
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Update `.env`:
   ```
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-16-char-app-password
   ```

**Option B: MailHog** (fake SMTP server):
```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```
Update `.env`:
```
MAIL_HOST=localhost
MAIL_PORT=1025
```
View emails: http://localhost:8025

---

## Common Development Tasks

### Add New Food Category

```bash
# Via Thymeleaf admin panel:
# 1. Login as admin: http://localhost:8080/admin
# 2. Navigate to Categories
# 3. Click "Add Category"
# 4. Fill form: Name, Description, Display Order
# 5. Upload image (optional)
# 6. Submit

# Via API (for testing):
curl -X POST http://localhost:8080/api/v1/admin/categories \
  -H "Content-Type: application/json" \
  -b "JSESSIONID=..." \
  -d '{"name":"Salads","description":"Fresh salads","displayOrder":3}'
```

### Add New Food Item

```bash
# Via admin panel:
# 1. Login as admin
# 2. Navigate to Food Items
# 3. Click "Add Item"
# 4. Fill form: Name, Description, Price, Category
# 5. Upload images (original + thumbnail auto-generated)
# 6. Submit
```

### Create Admin User

```bash
# Via SQL (for first admin):
docker-compose exec postgres psql -U foodshop_user -d foodshop -c "UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';"

# Via application (if admin registration endpoint exists):
curl -X POST http://localhost:8080/api/v1/admin/users/promote \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'
```

---

## Troubleshooting

### Application Fails to Start

**Error**: `Connection refused: postgres:5432`

**Solution**:
```bash
# Verify PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

---

### OAuth2 Login Fails

**Error**: `redirect_uri_mismatch`

**Solution**:
- Verify redirect URI in Google/Facebook console matches `http://localhost:8080/login/oauth2/code/{provider}`
- Ensure `.env` has correct client ID/secret
- Clear browser cookies and try again

---

### Stripe Webhook Not Received

**Error**: Order stays PENDING after payment

**Solution**:
1. Verify webhook endpoint is registered in Stripe Dashboard
2. Use Stripe CLI to forward webhooks:
   ```bash
   stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe
   ```
3. Check application logs for webhook signature verification errors
4. Verify `STRIPE_WEBHOOK_SECRET` in `.env` matches Stripe Dashboard

---

### MinIO Images Not Loading

**Error**: 404 on image URLs

**Solution**:
```bash
# Verify MinIO bucket exists
docker-compose exec minio mc ls minio/food-images

# Create bucket with public-read policy
docker-compose exec minio mc mb minio/food-images
docker-compose exec minio mc anonymous set download minio/food-images
```

---

### Tests Fail with Testcontainers

**Error**: `Could not find a valid Docker environment`

**Solution**:
- Ensure Docker Desktop is running
- Verify Docker daemon is accessible: `docker ps`
- On Linux, add user to docker group: `sudo usermod -aG docker $USER`
- Restart terminal/IDE

---

## User Story 1: Browse and Order Food Items - Testing Guide

This section provides specific instructions for testing User Story 1 functionality (catalog browsing, cart operations, checkout, and payment).

### Prerequisites

1. **Start all infrastructure services**:
   ```bash
   docker-compose up -d postgres redis minio
   ```

2. **Load seed data**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   The seed data migration (V900__seed_initial_data.sql) will run automatically, creating:
   - 5 categories (Pizza, Burgers, Salads, Desserts, Beverages)
   - 15 sample food items with prices and ratings

3. **Configure OAuth2 providers** (see OAuth2 Setup section above)

4. **Configure Stripe test mode** (see Stripe Payment Testing section above)

### Test Scenario: Complete Purchase Flow

#### Step 1: Browse Catalog

1. Navigate to http://localhost:8080
2. Verify homepage displays:
   - Hero section with "Welcome to Food Shop"
   - Category quick links (5 categories)
   - Featured items grid (8 items with ratings ≥4.5 and ≥5 reviews)

3. Click "Browse Catalog" button
4. Verify catalog page displays:
   - Sidebar with categories and search box
   - Food items grid (12 items per page)
   - Pagination controls (if >12 items)

5. Test category filter:
   - Click "Pizza" in sidebar
   - Verify only pizza items displayed
   - URL shows `?categoryId=1`

6. Test search:
   - Enter "burger" in search box
   - Click search button
   - Verify only burger items displayed
   - URL shows `?search=burger`

#### Step 2: View Food Item Details

1. Click any food item card
2. Verify detail page displays:
   - Item name, description, price
   - Category badge
   - Rating (stars and count)
   - Availability status badge
   - Quantity selector (1-99)
   - "Add to Cart" button (if authenticated)
   - "Please login to add items to cart" message (if not authenticated)

#### Step 3: Login with OAuth2

1. Click "Login" in navbar (if not authenticated)
2. Select OAuth2 provider (Google or Facebook)
3. Complete OAuth2 flow
4. Verify:
   - Redirected back to application
   - Navbar shows user name and "Logout" link
   - Cart icon appears in navbar

#### Step 4: Add Items to Cart

1. On food detail page, set quantity to 2
2. Click "Add to Cart" button
3. Verify:
   - Success message appears
   - Cart badge shows "2" items
   - Loading spinner displays during AJAX request

4. Add another item with quantity 1
5. Verify cart badge updates to "3" items

#### Step 5: View and Manage Cart

1. Click cart icon in navbar
2. Verify cart page displays:
   - All added items with thumbnails
   - Price per item and subtotal per row
   - Quantity controls (+/- buttons)
   - Remove button for each item
   - Order summary showing subtotal and total
   - "Proceed to Checkout" button
   - "Continue Shopping" button

3. Test quantity update:
   - Click "-" button on first item
   - Verify quantity decreases and subtotal updates
   - Verify page reloads showing updated cart

4. Test item removal:
   - Click trash icon on second item
   - Confirm removal (browser prompt)
   - Verify item removed and page reloads
   - Verify cart badge updates

5. Test clear cart:
   - Remove all items
   - Verify empty cart message displays
   - Verify "Start Shopping" button appears

#### Step 6: Checkout and Payment

1. Add items back to cart (for testing)
2. Click "Proceed to Checkout" button
3. Verify:
   - Loading spinner appears on button
   - Redirected to Stripe Checkout page
   - Order summary shows correct items and total
   - Stripe test mode banner appears

4. Fill payment details:
   - Email: test@example.com
   - Card number: `4242 4242 4242 4242`
   - Expiry: Any future date (e.g., 12/34)
   - CVC: Any 3 digits (e.g., 123)
   - Name: Test User

5. Click "Pay" button
6. Verify:
   - Payment processes successfully
   - Redirected to order success page

#### Step 7: Order Confirmation

1. On order success page, verify:
   - Green checkmark icon
   - "Order Confirmed!" heading
   - Order number (ORD-123)
   - Order status badge (CONFIRMED)
   - Total amount
   - Order date
   - "A confirmation email has been sent" message
   - "View Order Details" button
   - "View All Orders" button

2. Check application logs for:
   ```
   Sending order confirmation email to user@example.com
   Order 123 status updated to CONFIRMED
   ```

3. Click "View Order Details" button
4. Verify order detail page shows:
   - Order number and date
   - Status badge (CONFIRMED)
   - Items table with thumbnails, quantities, prices
   - Order summary with total
   - Status timeline (Order Placed → Payment Confirmed)

#### Step 8: Order History

1. Click "Orders" in navbar (or "View All Orders" button)
2. Verify order history page displays:
   - Order cards with order number, date, status, items count, total
   - Most recent orders first
   - Pagination controls (if >10 orders)
   - "View Details" button for each order

3. Click different order to verify details load correctly

4. Test ownership verification:
   - Logout and login as different user
   - Try to access previous order URL directly: http://localhost:8080/orders/{previous-order-id}
   - Verify access denied or 404 error (users can only view their own orders)

### Test Webhook Handling (Stripe CLI)

For local webhook testing, use Stripe CLI:

1. Install Stripe CLI:
   ```bash
   brew install stripe/stripe-cli/stripe  # macOS
   # or download from https://stripe.com/docs/stripe-cli
   ```

2. Login to Stripe:
   ```bash
   stripe login
   ```

3. Forward webhooks to local application:
   ```bash
   stripe listen --forward-to localhost:8080/api/webhook/stripe
   ```

4. Complete a test purchase (Steps 1-6 above)

5. Verify in Stripe CLI output:
   ```
   [200] POST /api/webhook/stripe [evt_xxx]
   → checkout.session.completed
   ```

6. Check application logs:
   ```
   Received Stripe webhook: checkout.session.completed
   Confirming order 123 from Stripe session cs_xxx
   Order 123 confirmed successfully
   ```

### Testing Error Scenarios

#### Test: Payment Decline

1. Add items to cart and proceed to checkout
2. Use declined test card: `4000 0000 0000 0002`
3. Verify:
   - Payment fails at Stripe
   - User stays on Stripe page with error message
   - Order remains PENDING in database
   - Cart is NOT cleared

#### Test: Cart with Unavailable Item

1. As admin, mark a food item as unavailable:
   ```bash
   docker-compose exec postgres psql -U foodshop_user -d foodshop -c "UPDATE food_items SET available = false WHERE id = 1;"
   ```

2. Try to add unavailable item to cart
3. Verify error message: "This item is currently unavailable"

#### Test: Empty Cart Checkout

1. Clear cart completely
2. Navigate directly to checkout: http://localhost:8080/orders/create
3. Verify redirected to cart page with error message

### Automated Testing

Run the automated test suite for User Story 1:

```bash
# Unit tests for services and controllers
./mvnw test -Dtest=Category*Test,FoodItem*Test,Cart*Test,Order*Test,Payment*Test

# Integration tests
./mvnw test -Dtest=CartIntegrationTest,OrderIntegrationTest

# E2E test (requires Chrome)
./mvnw test -Dtest=BrowseAndOrderE2ETest
```

Expected output:
```
Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
```

---

## Performance Testing

### Load Testing (Apache JMeter)

```bash
# Install JMeter
brew install jmeter  # macOS
# or download from https://jmeter.apache.org/

# Run test plan (50 concurrent users, 100 requests each)
jmeter -n -t load-test.jmx -l results.jtl -e -o report/

# View report
open report/index.html
```

**Expected Performance** (local dev environment):
- Catalog browse: p95 < 500ms
- Order creation: p95 < 2s
- Cart operations: p95 < 200ms

---

## Deployment (Docker Compose Production)

### Build Production Image

```bash
docker-compose -f docker-compose.prod.yml build
```

### Run Production Stack

```bash
docker-compose -f docker-compose.prod.yml up -d
```

**Production differences**:
- PostgreSQL with persistent volume
- Redis with AOF persistence
- MinIO with persistent volume
- Application with production profile (caching enabled, verbose logging disabled)
- Nginx reverse proxy (optional)

---

## IDE Setup

### IntelliJ IDEA

1. **Import Project**:
   - File → Open → Select `pom.xml`
   - Import as Maven project

2. **Configure Java 21**:
   - File → Project Structure → Project SDK → Add JDK → Select Java 21

3. **Enable Annotation Processing** (for Lombok, if used):
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable

4. **Install Plugins**:
   - Google Java Format
   - Thymeleaf
   - Docker

5. **Configure Code Style**:
   - Settings → Editor → Code Style → Java → Import Scheme → `checkstyle.xml`

6. **Run Configuration**:
   - Run → Edit Configurations → Add New → Spring Boot
   - Main class: `com.foodshop.FoodShopApplication`
   - VM options: `-Dspring.profiles.active=dev`
   - Environment variables: Load from `.env`

---

### VS Code

1. **Install Extensions**:
   - Extension Pack for Java (Microsoft)
   - Spring Boot Extension Pack (Pivotal)
   - Thymeleaf Syntax
   - Docker

2. **Configure Launch** (`.vscode/launch.json`):
```json
{
  "type": "java",
  "name": "Food Shop App",
  "request": "launch",
  "mainClass": "com.foodshop.FoodShopApplication",
  "projectName": "food-shop",
  "args": "--spring.profiles.active=dev",
  "envFile": "${workspaceFolder}/.env"
}
```

---

## Useful Commands Reference

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f app

# Run specific service
docker-compose up postgres redis

# Clean everything (including volumes)
docker-compose down -v

# Rebuild application
./mvnw clean package -DskipTests

# Run with debug
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Database backup
docker-compose exec postgres pg_dump -U foodshop_user foodshop > backup.sql

# Database restore
docker-compose exec -T postgres psql -U foodshop_user foodshop < backup.sql
```

---

## Next Steps

1. **Read Architecture Documentation**: See [research.md](research.md) for technical decisions
2. **Review Data Model**: See [data-model.md](data-model.md) for database schema
3. **Understand API Contracts**: See [contracts/README.md](contracts/README.md) for API endpoints
4. **Start Implementing**: Run `/speckit.tasks` to generate implementation task breakdown

---

## Support & Resources

- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/docs/3.2.x/reference/html/
- **Spring Security OAuth2**: https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html
- **Thymeleaf Documentation**: https://www.thymeleaf.org/documentation.html
- **Stripe API Reference**: https://stripe.com/docs/api
- **Testcontainers**: https://www.testcontainers.org/
- **Flyway Migrations**: https://flywaydb.org/documentation/

For questions or issues, contact the development team or create an issue in the repository.
