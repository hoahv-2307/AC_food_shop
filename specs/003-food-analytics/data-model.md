# Data Model: Food Analytics Dashboard and Monthly Reporting

**Feature**: Food Analytics Dashboard and Monthly Reporting  
**Date**: February 6, 2026  
**Phase**: 1 - Data Model Design

## Overview

This document defines the database schema for the food analytics feature using JPA/Hibernate entities mapped to PostgreSQL tables. The model supports tracking view counts and order counts for food items, along with monthly report generation tracking.

## Entity Relationship Diagram

```
┌──────────────┐         ┌──────────────────┐
│  FoodItem    │1       1│  FoodAnalytics   │
│──────────────│◄────────│──────────────────│
│ id (PK)      │         │ id (PK)          │
│ name         │         │ food_item_id(FK) │
│ description  │         │ view_count       │
│ price        │         │ order_count      │
│ ...          │         │ created_at       │
│ (existing)   │         │ updated_at       │
└──────────────┘         │ version          │
                         └──────────────────┘

┌──────────────────┐
│  MonthlyReport   │
│──────────────────│
│ id (PK)          │
│ report_date      │     Reports when monthly analytics 
│ generated_at     │     emails were sent (audit trail)
│ status           │
│ total_items      │
│ total_views      │
│ total_orders     │
│ error_message    │
└──────────────────┘
```

## Core Entities

### 1. FoodAnalytics

Tracks cumulative view counts and order counts for each food item.

**JPA Entity**:
```java
package com.foodshop.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "food_analytics",
    indexes = {
        @Index(name = "idx_food_analytics_food_item", columnList = "food_item_id", unique = true),
        @Index(name = "idx_food_analytics_view_count", columnList = "view_count DESC"),
        @Index(name = "idx_food_analytics_order_count", columnList = "order_count DESC")
    }
)
public class FoodAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "food_item_id", nullable = false, unique = true)
    private Long foodItemId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", insertable = false, updatable = false)
    private FoodItem foodItem;
    
    @NotNull
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;
    
    @NotNull
    @Column(name = "order_count", nullable = false)
    private Long orderCount = 0L;
    
    @Version
    @Column(name = "version")
    private Long version; // Optimistic locking for concurrent updates
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters, setters, equals, hashCode
}
```

**Database Schema** (Flyway: `V007__create_food_analytics_table.sql`):
```sql
CREATE TABLE food_analytics (
    id BIGSERIAL PRIMARY KEY,
    food_item_id BIGINT NOT NULL UNIQUE,
    view_count BIGINT NOT NULL DEFAULT 0,
    order_count BIGINT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_food_analytics_food_item 
        FOREIGN KEY (food_item_id) 
        REFERENCES food_items(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_food_analytics_food_item ON food_analytics(food_item_id);
CREATE INDEX idx_food_analytics_view_count ON food_analytics(view_count DESC);
CREATE INDEX idx_food_analytics_order_count ON food_analytics(order_count DESC);

-- Initialize analytics for all existing food items
INSERT INTO food_analytics (food_item_id, view_count, order_count)
SELECT id, 0, 0 FROM food_items
ON CONFLICT (food_item_id) DO NOTHING;

COMMENT ON TABLE food_analytics IS 'Tracks cumulative view and order counts for food items';
COMMENT ON COLUMN food_analytics.version IS 'Optimistic locking version for concurrent updates';
```

**Field Descriptions**:
- `id`: Primary key (auto-generated)
- `food_item_id`: Foreign key to food_items table (unique constraint ensures one-to-one)
- `view_count`: Cumulative count of food detail page views (session-deduplicated)
- `order_count`: Cumulative count of completed orders containing this food item
- `version`: JPA optimistic locking version to handle concurrent counter increments
- `created_at`: Timestamp when analytics record was created
- `updated_at`: Timestamp of last update (automatically updated on each increment)

**Validation Rules**:
- `food_item_id` must reference an existing food item
- `view_count` and `order_count` cannot be negative (application-enforced)
- Unique constraint on `food_item_id` ensures one analytics record per food item

**State Transitions**:
- CREATED: Analytics record initialized with 0 counts (via migration or food item creation)
- UPDATED: Counts incremented on view tracking or order completion events
- No deletion state - analytics persist even if food item is soft-deleted

---

### 2. MonthlyReport

Tracks monthly report generation status for auditing and preventing duplicate sends.

**JPA Entity**:
```java
package com.foodshop.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "monthly_reports",
    indexes = {
        @Index(name = "idx_monthly_report_date", columnList = "report_date", unique = true)
    }
)
public class MonthlyReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate; // First day of the month being reported (e.g., 2026-02-01)
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;
    
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
    
    @Column(name = "total_items")
    private Integer totalItems;
    
    @Column(name = "total_views")
    private Long totalViews;
    
    @Column(name = "total_orders")
    private Long totalOrders;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters, setters, equals, hashCode
}

enum ReportStatus {
    PENDING,    // Report generation scheduled but not started
    GENERATING, // Report generation in progress
    SENT,       // Report successfully generated and emailed
    FAILED      // Report generation or sending failed
}
```

**Database Schema** (Flyway: `V008__create_monthly_reports_table.sql`):
```sql
CREATE TABLE monthly_reports (
    id BIGSERIAL PRIMARY KEY,
    report_date DATE NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    generated_at TIMESTAMP,
    total_items INTEGER,
    total_views BIGINT,
    total_orders BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'GENERATING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_monthly_report_date ON monthly_reports(report_date);

COMMENT ON TABLE monthly_reports IS 'Tracks monthly analytics report generation and delivery status';
COMMENT ON COLUMN monthly_reports.report_date IS 'First day of the month being reported (e.g., 2026-02-01 for February 2026 report)';
```

**Field Descriptions**:
- `id`: Primary key (auto-generated)
- `report_date`: Date representing the month (first day of month, e.g., 2026-02-01)
- `status`: Current report status (PENDING, GENERATING, SENT, FAILED)
- `generated_at`: Timestamp when report was successfully generated and sent
- `total_items`: Count of food items included in report (cached for quick reference)
- `total_views`: Sum of all view counts in report (cached)
- `total_orders`: Sum of all order counts in report (cached)
- `error_message`: Error details if report generation/sending failed
- `created_at`: Timestamp when report record was created

**Validation Rules**:
- `report_date` must be unique (one report per month)
- `status` must be one of the enum values
- `generated_at` required when status is SENT
- `error_message` required when status is FAILED

**State Transitions**:
1. PENDING → GENERATING: When scheduled task starts report generation
2. GENERATING → SENT: When email successfully sent to all admins
3. GENERATING → FAILED: When generation or sending encounters errors
4. FAILED → GENERATING: When manual retry is triggered (future enhancement)

---

## Data Transfer Objects (DTOs)

### FoodAnalyticsDTO

Used for dashboard display to avoid loading full FoodItem entities.

```java
package com.foodshop.dto;

public record FoodAnalyticsDTO(
    Long foodItemId,
    String foodItemName,
    String imageUrl,
    Long viewCount,
    Long orderCount
) {
    // Compact constructor for validation
    public FoodAnalyticsDTO {
        if (viewCount == null) viewCount = 0L;
        if (orderCount == null) orderCount = 0L;
    }
}
```

### MonthlyReportSummaryDTO

Used for email template context.

```java
package com.foodshop.dto;

import java.time.YearMonth;
import java.util.List;

public record MonthlyReportSummaryDTO(
    YearMonth reportMonth,
    int totalItems,
    long totalViews,
    long totalOrders,
    List<FoodAnalyticsDTO> items
) {}
```

---

## Repository Interfaces

### FoodAnalyticsRepository

```java
package com.foodshop.repository;

import com.foodshop.domain.FoodAnalytics;
import com.foodshop.dto.FoodAnalyticsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodAnalyticsRepository extends JpaRepository<FoodAnalytics, Long> {
    
    Optional<FoodAnalytics> findByFoodItemId(Long foodItemId);
    
    @Query("SELECT new com.foodshop.dto.FoodAnalyticsDTO(" +
           "f.id, f.name, f.imageUrl, COALESCE(a.viewCount, 0), COALESCE(a.orderCount, 0)) " +
           "FROM FoodItem f LEFT JOIN f.analytics a " +
           "ORDER BY a.viewCount DESC NULLS LAST")
    List<FoodAnalyticsDTO> findAllFoodAnalyticsSortedByViews();
    
    @Query("SELECT new com.foodshop.dto.FoodAnalyticsDTO(" +
           "f.id, f.name, f.imageUrl, COALESCE(a.viewCount, 0), COALESCE(a.orderCount, 0)) " +
           "FROM FoodItem f LEFT JOIN f.analytics a " +
           "ORDER BY a.orderCount DESC NULLS LAST")
    List<FoodAnalyticsDTO> findAllFoodAnalyticsSortedByOrders();
    
    @Query("SELECT SUM(a.viewCount) FROM FoodAnalytics a")
    Long sumAllViewCounts();
    
    @Query("SELECT SUM(a.orderCount) FROM FoodAnalytics a")
    Long sumAllOrderCounts();
}
```

### MonthlyReportRepository

```java
package com.foodshop.repository;

import com.foodshop.domain.MonthlyReport;
import com.foodshop.domain.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {
    
    Optional<MonthlyReport> findByReportDate(LocalDate reportDate);
    
    boolean existsByReportDateAndStatus(LocalDate reportDate, ReportStatus status);
}
```

---

## Integration with Existing Entities

### FoodItem Entity Update

Add bidirectional relationship to `FoodAnalytics` (optional, for convenience):

```java
// Add to existing FoodItem entity
@OneToOne(mappedBy = "foodItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private FoodAnalytics analytics;
```

**Note**: This is optional and can be added in a later iteration if bidirectional navigation is needed. For Phase 1, unidirectional from FoodAnalytics to FoodItem is sufficient.

---

## Data Integrity Constraints

### Referential Integrity
- `food_analytics.food_item_id` → `food_items.id` (CASCADE delete)
- If a food item is deleted, its analytics are also deleted

### Business Rules (Application-Enforced)
- View counts and order counts must never be negative
- Analytics records must exist for all active food items
- Monthly reports must be unique per month (database-enforced via unique index)

### Concurrent Update Safety
- `@Version` column provides optimistic locking
- Retry logic in service layer handles `OptimisticLockException`
- Maximum 3 retry attempts with 100ms backoff

---

## Performance Considerations

### Indexes
- `food_item_id` (unique): Fast lookup for analytics by food item
- `view_count DESC`: Optimizes "most viewed" sorting
- `order_count DESC`: Optimizes "most ordered" sorting
- `report_date`: Fast lookup to prevent duplicate monthly reports

### Query Optimization
- Use JPA constructor expressions to fetch only needed fields
- LEFT JOIN ensures food items without analytics still appear
- `@Transactional(readOnly = true)` for dashboard queries
- Consider adding denormalized `food_item_name` if JOIN becomes bottleneck

### Scaling Considerations
- Current design supports 1000+ food items efficiently
- If item count exceeds 10,000, consider pagination on dashboard
- Analytics updates are distributed (not concentrated), low contention expected
- PostgreSQL's MVCC handles concurrent reads well

---

## Testing Data

### Sample FoodAnalytics Records
```sql
-- For testing/demo purposes
INSERT INTO food_analytics (food_item_id, view_count, order_count) VALUES
(1, 1250, 340),  -- Pizza Margherita
(2, 890, 210),   -- Cheeseburger
(3, 2100, 580),  -- Caesar Salad (most popular)
(4, 450, 95),    -- Chicken Wings
(5, 320, 72);    -- Chocolate Cake
```

### Sample MonthlyReport Records
```sql
-- For testing/demo purposes
INSERT INTO monthly_reports (report_date, status, generated_at, total_items, total_views, total_orders) VALUES
('2026-01-01', 'SENT', '2026-01-31 23:05:00', 150, 45000, 12500),
('2026-02-01', 'PENDING', NULL, NULL, NULL, NULL);
```

---

## Migration Checklist

- [x] V007: Create `food_analytics` table with indexes
- [x] V007: Initialize analytics for existing food items
- [x] V008: Create `monthly_reports` table with indexes
- [ ] Future: Add trigger to auto-create analytics on food item insert (optional)
- [ ] Future: Consider partitioning `monthly_reports` by year if retention is long

---

## Next Steps

1. Define API contracts for analytics dashboard endpoints
2. Create Thymeleaf template for monthly report email
3. Implement service layer with TDD approach
4. Add integration tests with Testcontainers
