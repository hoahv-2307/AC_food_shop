# API Contracts: Food Analytics Dashboard

**Feature**: Food Analytics Dashboard and Monthly Reporting  
**Date**: February 6, 2026  
**Phase**: 1 - API Contract Design

## Overview

This document defines the HTTP endpoints for the food analytics feature. All endpoints are server-side rendered using Spring MVC and Thymeleaf templates, following the existing architectural pattern of the AC-Food-Shop application.

## Authentication & Authorization

**Security Requirements**:
- All analytics endpoints require authentication (Spring Security)
- Access restricted to users with `ROLE_ADMIN` authority
- Unauthorized access returns HTTP 403 Forbidden

**Implementation**:
```java
@PreAuthorize("hasRole('ADMIN')")
```

---

## Endpoints

### 1. Analytics Dashboard - List All Food Analytics

**Purpose**: Display analytics dashboard with view/order counts for all food items.

**Endpoint**:
```
GET /admin/analytics
```

**Authentication**: Required (ROLE_ADMIN)

**Query Parameters**:
| Parameter | Type   | Required | Default      | Description                          |
|-----------|--------|----------|--------------|--------------------------------------|
| sort      | String | No       | views-desc   | Sort order: `views-desc`, `views-asc`, `orders-desc`, `orders-asc` |

**Request Example**:
```
GET /admin/analytics?sort=orders-desc
```

**Response**: HTML page (Thymeleaf template: `admin/analytics.html`)

**Model Attributes**:
```java
model.addAttribute("analyticsData", List<FoodAnalyticsDTO>);
model.addAttribute("currentSort", String); // e.g., "views-desc"
model.addAttribute("totalViews", Long);
model.addAttribute("totalOrders", Long);
model.addAttribute("totalItems", Integer);
```

**Success Response** (HTTP 200 OK):
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" 
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/base}">
<head>
    <title>Food Analytics Dashboard</title>
</head>
<body>
<div layout:fragment="content">
    <h1>Food Analytics Dashboard</h1>
    
    <div class="summary">
        <p>Total Items: <span th:text="${totalItems}">0</span></p>
        <p>Total Views: <span th:text="${totalViews}">0</span></p>
        <p>Total Orders: <span th:text="${totalOrders}">0</span></p>
    </div>
    
    <div class="sorting">
        <a th:href="@{/admin/analytics(sort='views-desc')}">Most Viewed</a>
        <a th:href="@{/admin/analytics(sort='views-asc')}">Least Viewed</a>
        <a th:href="@{/admin/analytics(sort='orders-desc')}">Most Ordered</a>
        <a th:href="@{/admin/analytics(sort='orders-asc')}">Least Ordered</a>
    </div>
    
    <table class="analytics-table">
        <thead>
            <tr>
                <th>Food Item</th>
                <th>Image</th>
                <th>Views</th>
                <th>Orders</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="item : ${analyticsData}">
                <td th:text="${item.foodItemName}">Pizza</td>
                <td><img th:src="${item.imageUrl}" alt="Food image" width="50"></td>
                <td th:text="${item.viewCount}">100</td>
                <td th:text="${item.orderCount}">25</td>
            </tr>
        </tbody>
    </table>
</div>
</body>
</html>
```

**Error Responses**:
- **403 Forbidden**: User is not authenticated or lacks ADMIN role
  ```html
  <!-- Redirect to /error/403 -->
  ```

**Performance**:
- Response time target: < 3 seconds for 1000 items
- Single database query with JOIN
- No pagination in P1 (items displayed on single page)

---

### 2. Food Detail View (View Tracking)

**Purpose**: Existing food detail page enhanced with view tracking.

**Endpoint**:
```
GET /food/{id}
```

**Authentication**: Optional (tracking works for both authenticated and guest users)

**Path Parameters**:
| Parameter | Type | Description       |
|-----------|------|-------------------|
| id        | Long | Food item ID      |

**Request Example**:
```
GET /food/123
```

**Side Effect**:
- Increments view count in `food_analytics` table if this food ID not in session
- Adds food ID to `HttpSession` attribute `viewedFoodIds` (Set<Long>)
- Session-based deduplication prevents multiple counts from same user session

**Tracking Logic**:
```java
@GetMapping("/food/{id}")
public String viewFoodDetail(@PathVariable Long id, HttpSession session, Model model) {
    FoodItem food = foodService.findById(id);
    model.addAttribute("food", food);
    
    // View tracking (async)
    Set<Long> viewedIds = (Set<Long>) session.getAttribute("viewedFoodIds");
    if (viewedIds == null) {
        viewedIds = new HashSet<>();
        session.setAttribute("viewedFoodIds", viewedIds);
    }
    
    if (!viewedIds.contains(id)) {
        analyticsTrackingService.incrementViewCount(id); // Async method
        viewedIds.add(id);
    }
    
    return "food/detail";
}
```

**Response**: HTML page (existing template: `food/detail.html`)

**Performance**:
- View tracking overhead: < 50ms (async event handling)
- Does not block page rendering

---

### 3. Order Completion (Order Tracking)

**Purpose**: Existing order completion flow enhanced with order tracking.

**Endpoint**:
```
POST /orders/{id}/complete
```

**Authentication**: Required (order must belong to authenticated user)

**Side Effect**:
- Publishes `OrderCompletedEvent` after successful order completion
- Event listener asynchronously increments order counts for all food items in order

**Tracking Logic**:
```java
@Service
public class OrderService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        
        // Publish event for analytics
        eventPublisher.publishEvent(new OrderCompletedEvent(this, order));
    }
}

@Component
public class AnalyticsEventListener {
    @Async
    @EventListener
    @Transactional
    public void handleOrderCompleted(OrderCompletedEvent event) {
        Order order = event.getOrder();
        for (OrderItem item : order.getItems()) {
            analyticsTrackingService.incrementOrderCount(item.getFoodItem().getId());
        }
    }
}
```

**Performance**:
- Order tracking overhead: < 50ms (async event handling)
- Does not affect order completion response time

---

## Internal Service APIs (Non-HTTP)

### Monthly Report Generation

**Trigger**: Scheduled task (cron: `0 0 23 L * ?` - 11 PM on last day of month)

**Service Method**:
```java
@Service
public class MonthlyReportService {
    
    /**
     * Generates and sends monthly analytics report to all admin users.
     * Creates MonthlyReport record to track generation status.
     * 
     * @param reportMonth The month to generate report for (e.g., YearMonth.of(2026, 2))
     * @throws EmailSendException if email delivery fails
     */
    public void generateAndSendMonthlyReport(YearMonth reportMonth) {
        LocalDate reportDate = reportMonth.atDay(1);
        
        // Check if report already sent
        if (monthlyReportRepository.existsByReportDateAndStatus(
                reportDate, ReportStatus.SENT)) {
            log.info("Report for {} already sent, skipping", reportMonth);
            return;
        }
        
        // Create report record
        MonthlyReport report = new MonthlyReport();
        report.setReportDate(reportDate);
        report.setStatus(ReportStatus.GENERATING);
        monthlyReportRepository.save(report);
        
        try {
            // Gather analytics data
            List<FoodAnalyticsDTO> analytics = 
                foodAnalyticsRepository.findAllFoodAnalyticsSortedByViews();
            long totalViews = foodAnalyticsRepository.sumAllViewCounts();
            long totalOrders = foodAnalyticsRepository.sumAllOrderCounts();
            
            // Build email context
            MonthlyReportSummaryDTO summary = new MonthlyReportSummaryDTO(
                reportMonth,
                analytics.size(),
                totalViews,
                totalOrders,
                analytics
            );
            
            // Send to all admins
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            for (User admin : admins) {
                emailService.sendMonthlyAnalyticsReport(admin.getEmail(), summary);
            }
            
            // Update report status
            report.setStatus(ReportStatus.SENT);
            report.setGeneratedAt(LocalDateTime.now());
            report.setTotalItems(analytics.size());
            report.setTotalViews(totalViews);
            report.setTotalOrders(totalOrders);
            monthlyReportRepository.save(report);
            
        } catch (Exception e) {
            report.setStatus(ReportStatus.FAILED);
            report.setErrorMessage(e.getMessage());
            monthlyReportRepository.save(report);
            throw e;
        }
    }
}
```

---

## Email Contract

### Monthly Analytics Report Email

**Subject**: `Monthly Analytics Report - [Month Year]`

**To**: All users with `ROLE_ADMIN` (from `users` table)

**Content-Type**: `text/html; charset=UTF-8`

**Template**: `src/main/resources/templates/email/monthly-analytics-report.html`

**Template Context**:
```java
Context context = new Context();
context.setVariable("reportMonth", "February 2026");
context.setVariable("totalItems", 150);
context.setVariable("totalViews", 45000L);
context.setVariable("totalOrders", 12500L);
context.setVariable("analyticsData", List<FoodAnalyticsDTO>);
```

**Email Body Template**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { color: #333; }
        .summary { background: #f5f5f5; padding: 15px; margin: 20px 0; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th { background: #4CAF50; color: white; padding: 10px; text-align: left; }
        td { padding: 8px; border-bottom: 1px solid #ddd; }
        tr:hover { background: #f5f5f5; }
    </style>
</head>
<body>
    <h1>Monthly Analytics Report - <span th:text="${reportMonth}">February 2026</span></h1>
    
    <div class="summary">
        <p><strong>Summary for <span th:text="${reportMonth}">February 2026</span></strong></p>
        <p>Total Food Items: <span th:text="${totalItems}">0</span></p>
        <p>Total Views: <span th:text="${totalViews}">0</span></p>
        <p>Total Orders: <span th:text="${totalOrders}">0</span></p>
    </div>
    
    <h2>Detailed Analytics</h2>
    <table>
        <thead>
            <tr>
                <th>Food Item</th>
                <th>Views</th>
                <th>Orders</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="item : ${analyticsData}">
                <td th:text="${item.foodItemName}">Pizza</td>
                <td th:text="${item.viewCount}">100</td>
                <td th:text="${item.orderCount}">25</td>
            </tr>
        </tbody>
    </table>
    
    <p style="margin-top: 30px; color: #666; font-size: 12px;">
        This is an automated report from AC-Food-Shop Analytics System.
    </p>
</body>
</html>
```

**Delivery Requirements**:
- Must be sent to all admin users (no filtering)
- Delivery failures should be logged but not retry (one attempt per month)
- Email generation must complete within 5 minutes

---

## Navigation Integration

### Admin Navigation Menu

Add analytics link to existing admin navigation:

```html
<!-- In fragments/navigation.html -->
<nav th:fragment="admin-nav" sec:authorize="hasRole('ADMIN')">
    <ul>
        <li><a th:href="@{/admin/dashboard}">Dashboard</a></li>
        <li><a th:href="@{/admin/users}">Users</a></li>
        <li><a th:href="@{/admin/orders}">Orders</a></li>
        <li><a th:href="@{/admin/food-items}">Food Items</a></li>
        <li><a th:href="@{/admin/analytics}">Analytics</a></li> <!-- NEW -->
    </ul>
</nav>
```

---

## Error Handling

### Standard Error Responses

All endpoints follow existing error handling patterns:

**403 Forbidden** (Non-admin access):
```
Redirect to /error/403 (existing error page)
```

**404 Not Found** (Invalid food ID):
```
Redirect to /error/404 (existing error page)
```

**500 Internal Server Error** (Unexpected failures):
```
Redirect to /error/500 (existing error page)
Log full stack trace for debugging
```

---

## Testing Contracts

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AdminAnalyticsControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAnalyticsDashboard_returnsOk() throws Exception {
        mockMvc.perform(get("/admin/analytics"))
               .andExpect(status().isOk())
               .andExpect(view().name("admin/analytics"))
               .andExpect(model().attributeExists("analyticsData"))
               .andExpect(model().attributeExists("totalViews"))
               .andExpect(model().attributeExists("totalOrders"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testAnalyticsDashboard_nonAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(get("/admin/analytics"))
               .andExpect(status().isForbidden());
    }
}
```

---

## Performance Requirements

| Endpoint/Operation | Target | Monitoring |
|-------------------|--------|------------|
| GET /admin/analytics | < 3s for 1000 items | `@Timed("analytics.dashboard")` |
| View tracking overhead | < 50ms | `@Timed("analytics.track.view")` |
| Order tracking overhead | < 50ms | `@Timed("analytics.track.order")` |
| Monthly report generation | < 5 minutes | `@Timed("analytics.report.generate")` |

---

## Security Considerations

- **CSRF Protection**: All POST endpoints protected by Spring Security CSRF (existing)
- **XSS Prevention**: Thymeleaf auto-escapes all user-provided content
- **SQL Injection**: JPA parameterized queries prevent SQL injection
- **Authorization**: `@PreAuthorize` annotations enforce role-based access
- **Session Security**: Redis session store with secure configuration (existing)

---

## Contract Validation Checklist

- [x] All endpoints follow REST conventions
- [x] Authentication/authorization requirements documented
- [x] Request/response formats defined
- [x] Error handling specified
- [x] Performance targets documented
- [x] Security considerations addressed
- [x] Testing contracts provided

---

## Next Steps

1. Implement controller layer with TDD approach
2. Create Thymeleaf templates for dashboard and email
3. Add integration tests with Testcontainers
4. Configure scheduled task for monthly reports
5. Monitor performance metrics via Micrometer/Prometheus
