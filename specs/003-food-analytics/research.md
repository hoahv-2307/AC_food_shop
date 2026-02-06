# Research: Food Analytics Dashboard and Monthly Reporting

**Feature**: Food Analytics Dashboard and Monthly Reporting  
**Date**: February 6, 2026  
**Phase**: 0 - Research & Technical Decisions

## Overview

This document captures research findings and technical decisions for implementing the food analytics feature. All decisions are informed by the existing Spring Boot 3.2.2 / Java 21 technology stack and align with the project's constitution requirements for code quality, testing, UX consistency, and performance.

## Research Areas

### 1. View Tracking Strategy

**Question**: How to track food item views without duplicate counting from page refreshes?

**Decision**: Session-based tracking using Spring Session (Redis)

**Rationale**:
- Spring Session with Redis is already configured in the project
- Can store a Set of viewed food item IDs per session to deduplicate within a user session
- Low overhead (<50ms) as required by performance constraints
- Session expiry naturally handles cleanup of tracking data
- Alternative (cookie-based) rejected due to client-side manipulation risks
- Alternative (IP-based) rejected due to inaccuracy with proxies/NAT

**Implementation Approach**:
- Store `viewedFoodIds: Set<Long>` in session attributes
- On food detail page load, check if food ID is in session Set
- If not present, increment view count and add to Set
- Use Spring's `@SessionScope` or `HttpSession` API

**Best Practices**:
- Use Redis atomic operations for incrementing counters
- Keep session data minimal (only IDs, not full objects)
- Set appropriate session timeout (30 minutes default is reasonable)

---

### 2. Order Tracking Integration

**Question**: How to track completed orders without affecting order processing performance?

**Decision**: Event-driven tracking using Spring Application Events

**Rationale**:
- Spring's event publishing mechanism is non-blocking and asynchronous
- Decouples analytics tracking from core order processing logic
- Meets <50ms overhead requirement since event handling is async
- Existing order service can publish `OrderCompletedEvent` after order finalization
- Alternative (database triggers) rejected to keep logic in application layer
- Alternative (direct service call) rejected to avoid tight coupling

**Implementation Approach**:
- Create `OrderCompletedEvent` with order details
- Order service publishes event after successful order completion
- `AnalyticsTrackingService` listens with `@EventListener` and `@Async`
- Extract food items from order and increment their order counts

**Best Practices**:
- Use `@Async` with custom thread pool for analytics events
- Implement retry logic for transient failures
- Log analytics tracking failures without affecting order flow

---

### 3. Analytics Data Storage

**Question**: Should analytics be separate entities or added to existing FoodItem table?

**Decision**: Separate `FoodAnalytics` entity with one-to-one relationship to `FoodItem`

**Rationale**:
- Separation of concerns: analytics data is a distinct bounded context
- Prevents frequent updates to core `FoodItem` table (better for caching)
- Allows independent scaling and optimization of analytics queries
- Easier to implement data retention policies (e.g., archive old analytics)
- Alternative (columns on FoodItem) rejected due to schema bloat and poor separation
- Alternative (NoSQL store) rejected to maintain consistency with existing stack

**Schema Design**:
```sql
CREATE TABLE food_analytics (
    id BIGSERIAL PRIMARY KEY,
    food_item_id BIGINT NOT NULL UNIQUE,
    view_count BIGINT DEFAULT 0,
    order_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (food_item_id) REFERENCES food_items(id) ON DELETE CASCADE
);

CREATE INDEX idx_food_analytics_food_item ON food_analytics(food_item_id);
CREATE INDEX idx_food_analytics_view_count ON food_analytics(view_count DESC);
CREATE INDEX idx_food_analytics_order_count ON food_analytics(order_count DESC);
```

**Best Practices**:
- Use `BIGINT` for counters to prevent overflow
- Add indexes on count columns to support sorting (User Story 3)
- Use optimistic locking (`@Version`) to handle concurrent updates
- Consider denormalizing food_item_name in analytics for query performance

---

### 4. Monthly Report Scheduling

**Question**: How to schedule monthly report generation at end of month?

**Decision**: Spring's `@Scheduled` annotation with cron expression

**Rationale**:
- Spring Boot has built-in scheduling support, no additional dependencies needed
- Cron expressions provide flexible scheduling (last day of month support)
- Existing actuator endpoints can monitor scheduled task status
- Alternative (Quartz) rejected as overkill for single recurring task
- Alternative (external scheduler like cron) rejected to keep everything in application

**Implementation Approach**:
- Enable scheduling with `@EnableScheduling` in configuration
- Use cron expression: `0 0 23 L * ?` (11 PM on last day of month)
- Create `MonthlyReportScheduler` with `@Scheduled` method
- Method triggers `MonthlyReportService` to generate and send report

**Cron Expression**:
```java
@Scheduled(cron = "0 0 23 L * ?", zone = "UTC")
public void generateMonthlyReport() {
    // L = last day of month
    // 23:00 UTC = 11 PM UTC
}
```

**Best Practices**:
- Run at off-peak hours (11 PM suggested)
- Use UTC timezone for consistency across deployments
- Add `@Async` to prevent blocking if report generation takes >5 minutes
- Store report generation status in `monthly_reports` table for auditing

---

### 5. Email Report Format and Delivery

**Question**: What format should the monthly email use and how to ensure reliable delivery?

**Decision**: HTML email using Thymeleaf templates with table layout

**Rationale**:
- Thymeleaf is already used for web templates, reuse for email consistency
- HTML emails support better formatting (tables, styling) than plain text
- Spring Mail (already configured) supports HTML with inline CSS
- Alternative (PDF attachment) rejected due to added complexity (rendering library)
- Alternative (plain text) rejected due to poor readability for tabular data

**Implementation Approach**:
- Create Thymeleaf template: `src/main/resources/templates/email/monthly-analytics-report.html`
- Use table structure with food item name, view count, order count columns
- Include summary statistics (total views, total orders, top 5 items)
- Use `MimeMessageHelper` with `setText(html, true)` for HTML emails

**Email Template Structure**:
```html
<h1>Monthly Analytics Report - [Month Year]</h1>
<p>Summary: [total items], [total views], [total orders]</p>
<table>
  <tr><th>Food Item</th><th>Views</th><th>Orders</th></tr>
  <!-- Iterate food analytics -->
</table>
```

**Best Practices**:
- Inline CSS for email client compatibility
- Test with common email clients (Gmail, Outlook)
- Implement retry mechanism for failed email sends
- Log all email delivery attempts with success/failure status
- Consider batching if admin list is large (unlikely for this use case)

---

### 6. Dashboard Query Optimization

**Question**: How to ensure dashboard loads <3s for 1000 food items?

**Decision**: Single optimized JOIN query with JPA projection

**Rationale**:
- Single query with JOIN is faster than N+1 queries
- JPA projections reduce memory overhead by fetching only needed fields
- PostgreSQL can efficiently handle 1000-row JOINs with proper indexes
- Alternative (separate queries + in-memory join) rejected due to complexity
- Alternative (caching) deferred to post-MVP optimization if needed

**Implementation Approach**:
```java
@Query("SELECT new com.foodshop.dto.FoodAnalyticsDTO(f.name, f.imageUrl, a.viewCount, a.orderCount) " +
       "FROM FoodItem f LEFT JOIN f.analytics a ORDER BY a.viewCount DESC")
List<FoodAnalyticsDTO> findAllFoodAnalytics();
```

**Optimization Techniques**:
- Use JPA constructor expression to avoid entity loading overhead
- LEFT JOIN ensures food items without analytics still appear (with 0 counts)
- Index on `food_analytics.food_item_id` for fast JOIN
- Index on `food_analytics.view_count` and `order_count` for sorting
- Consider pagination if item count exceeds 1000 (not in P1 scope)

**Best Practices**:
- Monitor query execution time with Spring Boot Actuator metrics
- Add `@Transactional(readOnly = true)` for read-only query optimization
- Use EXPLAIN ANALYZE in PostgreSQL to validate query plan

---

### 7. Concurrent Counter Updates

**Question**: How to handle concurrent view/order count increments safely?

**Decision**: Optimistic locking with `@Version` and retry logic

**Rationale**:
- Optimistic locking is lighter than pessimistic locking (better performance)
- Conflicts are rare (analytics updates are distributed over time)
- JPA's `@Version` provides automatic optimistic lock support
- Alternative (pessimistic locking) rejected due to performance impact
- Alternative (database-level atomic operations) considered but JPA approach simpler

**Implementation Approach**:
```java
@Entity
public class FoodAnalytics {
    @Version
    private Long version; // JPA automatic optimistic locking
    
    private Long viewCount;
    private Long orderCount;
}

// Service layer
@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
public void incrementViewCount(Long foodItemId) {
    FoodAnalytics analytics = repository.findByFoodItemId(foodItemId);
    analytics.setViewCount(analytics.getViewCount() + 1);
    repository.save(analytics); // Throws OptimisticLockException on conflict
}
```

**Best Practices**:
- Use Spring Retry for automatic retry on `OptimisticLockException`
- Max 3 retries with 100ms backoff is reasonable for this use case
- Log retry attempts for monitoring contention patterns
- Consider atomic operations if contention becomes problematic (unlikely)

---

### 8. Analytics Initialization for Existing Food Items

**Question**: How to initialize analytics records for food items created before this feature?

**Decision**: Flyway migration with INSERT from FoodItem table

**Rationale**:
- Ensures all existing food items have analytics records from the start
- Avoids null checks or lazy initialization in application code
- One-time operation executed during deployment
- Alternative (lazy initialization) rejected due to complexity and potential race conditions

**Migration Script**:
```sql
-- V007__create_food_analytics_table.sql
CREATE TABLE food_analytics (...);

-- Initialize analytics for all existing food items
INSERT INTO food_analytics (food_item_id, view_count, order_count)
SELECT id, 0, 0 FROM food_items
ON CONFLICT (food_item_id) DO NOTHING;
```

**Best Practices**:
- Use `ON CONFLICT DO NOTHING` for idempotency
- Future food items will have analytics created via application logic
- Consider trigger or lifecycle callback to auto-create analytics on food item creation

---

## Technology Stack Summary

All decisions align with the existing AC-Food-Shop technology stack:

- **Framework**: Spring Boot 3.2.2, Spring MVC, Spring Security
- **Data Access**: Spring Data JPA, Hibernate, Flyway migrations
- **Database**: PostgreSQL (analytics tables), Redis (session tracking)
- **Scheduling**: Spring `@Scheduled` with cron expressions
- **Email**: Spring Mail with Thymeleaf HTML templates
- **Testing**: JUnit 5, Spring Boot Test, Testcontainers, Selenium
- **Monitoring**: Micrometer/Prometheus for performance metrics

No new dependencies required beyond what's already in `pom.xml`.

---

## Performance Validation Strategy

To ensure constitution performance requirements are met:

1. **View Tracking Overhead** (<50ms):
   - Use `@Timed` annotation from Micrometer
   - Monitor p95 latency via Prometheus
   - Test with concurrent users via load testing

2. **Dashboard Load Time** (<3s for 1000 items):
   - Use Spring Boot Actuator `/metrics` endpoint
   - Add custom timer for dashboard query
   - Test with 1000-item dataset in integration tests

3. **Email Generation** (<5 minutes):
   - Log generation start/end timestamps
   - Alert if generation exceeds 4 minutes (80% threshold)
   - Test with max expected dataset size

4. **Database Query Performance**:
   - Enable Hibernate SQL logging in test environment
   - Use EXPLAIN ANALYZE to validate index usage
   - Monitor slow query log in PostgreSQL

---

## Security Considerations

- **Dashboard Access**: Protected by Spring Security `@PreAuthorize("hasRole('ADMIN')")`
- **Analytics Data**: No PII stored, only aggregate counts
- **Email Recipients**: Only admin email addresses from User table where role = ADMIN
- **Session Tracking**: Redis session store already secured (no additional config needed)

---

## Alternatives Considered and Rejected

| Alternative | Why Rejected |
|-------------|--------------|
| Google Analytics integration | Requires external service, no order tracking, privacy concerns |
| Real-time dashboard updates (WebSockets) | Added complexity, not required by P1 user story |
| Separate microservice for analytics | Overkill for feature scope, increases operational complexity |
| NoSQL for analytics storage | Inconsistent with existing stack, adds new dependency |
| Manual monthly report triggering | Requirement specifies automated end-of-month delivery |
| CSV email attachment | Poorer readability than HTML table in email body |

---

## Open Questions (None)

All technical unknowns have been resolved through this research phase. No [NEEDS CLARIFICATION] items remain.

---

## Next Steps (Phase 1)

1. Design detailed data model with JPA entities and Flyway migrations
2. Define API contracts for admin analytics dashboard endpoints
3. Create quickstart guide for local development and testing
4. Update agent context with Spring Scheduling and email templating patterns
