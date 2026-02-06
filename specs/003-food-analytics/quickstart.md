# Quickstart Guide: Food Analytics Dashboard

**Feature**: Food Analytics Dashboard and Monthly Reporting  
**Date**: February 6, 2026  
**Branch**: `003-food-analytics`

## Overview

This guide helps developers set up and test the food analytics feature locally. Follow these steps to run the application with analytics tracking, view the dashboard, and test monthly report generation.

## Prerequisites

- Java 21 installed
- Docker and Docker Compose installed (for PostgreSQL, Redis, MailHog)
- Maven 3.8+ installed
- Git installed

## Quick Start

### 1. Clone and Switch to Feature Branch

```bash
git clone <repository-url>
cd AC-food-shop
git checkout 003-food-analytics
```

### 2. Start Infrastructure Services

```bash
# Start PostgreSQL, Redis, and MailHog (email testing)
docker compose up -d
```

Verify services are running:
```bash
docker compose ps
# Should show postgres, redis, and mailhog containers running
```

### 3. Run Database Migrations

Migrations run automatically on application startup, but you can verify:

```bash
# Check migration status
mvn flyway:info

# Expected migrations for analytics:
# V007__create_food_analytics_table.sql
# V008__create_monthly_reports_table.sql
```

### 4. Build and Run Application

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Or run the JAR
java -jar target/food-shop-1.0.0.jar
```

Application starts on `http://localhost:8080`

### 5. Access Analytics Dashboard

**Login as Admin**:
1. Open `http://localhost:8080/login`
2. Click "Login with Google" (uses OAuth2 mock in dev mode)
3. Navigate to `http://localhost:8080/admin/analytics`

**Expected Result**:
- Dashboard displays all food items with view/order counts (initially 0)
- Sorting options available (Most Viewed, Most Ordered, etc.)
- Summary statistics at top (Total Items, Total Views, Total Orders)

---

## Testing View Tracking

### Manual Testing

1. **Open food detail page** (as any user, logged in or guest):
   ```
   http://localhost:8080/food/1
   ```

2. **Refresh the analytics dashboard**:
   ```
   http://localhost:8080/admin/analytics
   ```

3. **Verify view count increased**:
   - View count for food item #1 should now be 1

4. **Refresh food detail page** (same session):
   - View count should NOT increase (session deduplication)

5. **Clear browser session and revisit**:
   - View count should increase again

### Automated Testing

```bash
# Run integration tests
mvn test -Dtest=AnalyticsIntegrationTest

# Run E2E tests (requires Chrome/ChromeDriver)
mvn test -Dtest=AnalyticsDashboardE2ETest
```

---

## Testing Order Tracking

### Manual Testing

1. **Add food items to cart**:
   ```
   http://localhost:8080/food/1
   Click "Add to Cart"
   ```

2. **Complete order**:
   ```
   http://localhost:8080/cart
   Click "Checkout"
   Complete payment (uses Stripe test mode)
   ```

3. **Check analytics dashboard**:
   - Order count for food item #1 should increase by 1

### Automated Testing

```bash
# Run order tracking tests
mvn test -Dtest=OrderCompletionAnalyticsTest
```

---

## Testing Monthly Report Generation

### Manual Trigger (Development)

Since monthly reports are scheduled for end of month, use the actuator endpoint to trigger manually:

```bash
# Trigger report generation for current month
curl -X POST http://localhost:8080/actuator/analytics/generate-report

# Or use the admin UI (if implemented):
# http://localhost:8080/admin/analytics/reports
```

### View Email in MailHog

1. Open MailHog web UI:
   ```
   http://localhost:8025
   ```

2. Check for email with subject "Monthly Analytics Report - [Month Year]"

3. Verify email contains:
   - Summary statistics (total items, views, orders)
   - Table with all food items and their counts
   - Proper HTML formatting

### Scheduled Testing

To test the actual scheduled task:

1. **Temporarily change cron expression** in `SchedulingConfig.java`:
   ```java
   // Change from:
   @Scheduled(cron = "0 0 23 L * ?")
   
   // To (runs every 5 minutes for testing):
   @Scheduled(cron = "0 */5 * * * ?")
   ```

2. **Wait for next execution** (5 minutes)

3. **Check logs**:
   ```bash
   tail -f logs/application.log | grep "Monthly report"
   ```

4. **Verify report in database**:
   ```sql
   SELECT * FROM monthly_reports ORDER BY created_at DESC LIMIT 1;
   ```

---

## Development Workflow

### TDD Cycle (Red-Green-Refactor)

1. **Write failing test**:
   ```bash
   # Create test file
   src/test/java/com/foodshop/service/AnalyticsTrackingServiceTest.java
   
   # Run test (should fail)
   mvn test -Dtest=AnalyticsTrackingServiceTest
   ```

2. **Implement feature**:
   ```bash
   # Create service file
   src/main/java/com/foodshop/service/AnalyticsTrackingService.java
   
   # Run test (should pass)
   mvn test -Dtest=AnalyticsTrackingServiceTest
   ```

3. **Refactor**:
   - Clean up code
   - Re-run tests to ensure still passing

### Running Checkstyle

```bash
# Run code quality checks
mvn checkstyle:check

# Fix common issues automatically (if using IDE plugin)
# Or manually follow checkstyle.xml rules
```

### Running All Tests

```bash
# Unit tests only
mvn test

# Integration tests only
mvn verify -DskipUnitTests

# All tests (unit + integration + E2E)
mvn verify

# With coverage report
mvn verify jacoco:report
# View coverage: target/site/jacoco/index.html
```

---

## Database Inspection

### Connect to PostgreSQL

```bash
# Using Docker exec
docker compose exec postgres psql -U foodshop -d foodshop

# Or using psql client directly
psql -h localhost -p 5432 -U foodshop -d foodshop
```

### Useful SQL Queries

```sql
-- View all analytics records
SELECT fa.id, fi.name, fa.view_count, fa.order_count, fa.updated_at
FROM food_analytics fa
JOIN food_items fi ON fa.food_item_id = fi.id
ORDER BY fa.view_count DESC;

-- View monthly report history
SELECT * FROM monthly_reports ORDER BY report_date DESC;

-- Check total views and orders
SELECT 
    COUNT(*) as total_items,
    SUM(view_count) as total_views, 
    SUM(order_count) as total_orders
FROM food_analytics;

-- Find food items without analytics (should be none)
SELECT fi.id, fi.name 
FROM food_items fi
LEFT JOIN food_analytics fa ON fi.id = fa.food_item_id
WHERE fa.id IS NULL;
```

---

## Performance Testing

### Load Testing View Tracking

```bash
# Install Apache Bench (if not installed)
sudo apt-get install apache2-utils  # Ubuntu/Debian
brew install ab                      # macOS

# Load test view tracking (100 requests, 10 concurrent)
ab -n 100 -c 10 http://localhost:8080/food/1

# Check p95 latency is < 50ms overhead
# (Compare with baseline food detail page load time)
```

### Load Testing Dashboard

```bash
# Load test dashboard (100 requests, 10 concurrent)
ab -n 100 -c 10 -C "JSESSIONID=<session-id>" http://localhost:8080/admin/analytics

# Verify p95 < 3 seconds for 1000 items
```

### Monitoring Metrics

```bash
# View Prometheus metrics
curl http://localhost:8080/actuator/prometheus | grep analytics

# Expected metrics:
# - analytics_track_view_seconds_count
# - analytics_track_view_seconds_sum
# - analytics_dashboard_seconds_count
# - analytics_report_generate_seconds_count
```

---

## Troubleshooting

### Issue: View count not incrementing

**Symptoms**: Food detail page loads but view count stays at 0

**Diagnosis**:
```bash
# Check logs for analytics tracking errors
grep "Analytics" logs/application.log

# Verify Redis is running
docker compose ps redis

# Check session storage
redis-cli
> KEYS *session*
> GET <session-key>
```

**Solution**:
- Restart Redis: `docker compose restart redis`
- Clear browser cookies and retry
- Check if `@Async` is properly configured in `AsyncConfig.java`

---

### Issue: Monthly report not sending

**Symptoms**: Report status is FAILED in database

**Diagnosis**:
```sql
SELECT * FROM monthly_reports WHERE status = 'FAILED';
-- Check error_message column
```

**Common Causes**:
1. **MailHog not running**: `docker compose up -d mailhog`
2. **No admin users**: Insert test admin in database
3. **Email template error**: Check Thymeleaf template syntax

**Solution**:
```bash
# Restart MailHog
docker compose restart mailhog

# Create test admin user
psql -h localhost -U foodshop -d foodshop -c \
  "UPDATE users SET role = 'ADMIN' WHERE email = 'test@example.com';"

# Check email service logs
grep "EmailService" logs/application.log
```

---

### Issue: Dashboard loads slowly (>3s)

**Symptoms**: Dashboard page takes longer than 3 seconds

**Diagnosis**:
```sql
-- Check query execution plan
EXPLAIN ANALYZE 
SELECT f.name, f.image_url, COALESCE(a.view_count, 0), COALESCE(a.order_count, 0)
FROM food_items f 
LEFT JOIN food_analytics a ON f.id = a.food_item_id
ORDER BY a.view_count DESC NULLS LAST;
```

**Solution**:
- Verify indexes exist: `\d food_analytics` in psql
- Check for missing indexes on sort columns
- Consider adding food_item_name to food_analytics (denormalization)

---

### Issue: Concurrent update conflicts

**Symptoms**: `OptimisticLockException` in logs

**Diagnosis**:
```bash
# Check retry logs
grep "OptimisticLockException" logs/application.log

# If frequent, indicates high contention
```

**Solution**:
- Verify `@Retryable` is configured with proper backoff
- Consider increasing retry attempts or backoff delay
- If persistent, switch to pessimistic locking (rare, should not be needed)

---

## Environment Configuration

### Development (`application-dev.yml`)

```yaml
spring:
  # View tracking enabled
  analytics:
    view-tracking:
      enabled: true
      session-timeout: 30m
  
  # Email via MailHog
  mail:
    host: localhost
    port: 1025
  
  # Scheduling enabled
  task:
    scheduling:
      enabled: true
```

### Test (`application-test.yml`)

```yaml
spring:
  # Use test cron (runs every minute)
  analytics:
    report-cron: "0 * * * * ?"
  
  # Use Testcontainers
  datasource:
    url: ${testcontainers.postgresql.jdbc-url}
```

---

## Next Steps

After setup and testing:

1. **Review generated code** against specification
2. **Run full test suite** and verify 80% coverage
3. **Test in staging environment** with production-like data
4. **Perform load testing** to validate performance requirements
5. **Deploy to production** following standard deployment process

---

## Useful Commands Cheat Sheet

```bash
# Start services
docker compose up -d

# Stop services
docker compose down

# View logs
docker compose logs -f

# Run application
mvn spring-boot:run

# Run tests
mvn verify

# Check code quality
mvn checkstyle:check

# View email (MailHog)
open http://localhost:8025

# View metrics (Prometheus)
open http://localhost:8080/actuator/prometheus

# Connect to database
docker compose exec postgres psql -U foodshop -d foodshop

# Connect to Redis
docker compose exec redis redis-cli

# Trigger manual report
curl -X POST http://localhost:8080/actuator/analytics/generate-report
```

---

## Support

For issues or questions:
1. Check troubleshooting section above
2. Review logs in `logs/application.log`
3. Consult [spec.md](spec.md) for requirements
4. Consult [data-model.md](data-model.md) for schema details
5. Consult [contracts/README.md](contracts/README.md) for API details
