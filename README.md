# AC Food Shop

An agentic coding project built with SpecKit - A full-featured e-commerce food ordering platform with admin analytics.

## Features

### Customer Features
- Browse food catalog with categories and search
- Add items to cart with real-time updates
- Secure checkout with Stripe integration
- Order tracking and history
- OAuth2 authentication (Google, GitHub)
- Rate and review purchased items

### Admin Features
- **Analytics Dashboard** 🎯
  - Real-time view and order tracking for all food items
  - Sortable metrics (most viewed, most ordered)
  - Conversion rate analytics
  - Session-based view deduplication
- **Automated Monthly Reports** 📊
  - Email reports sent automatically on last day of month
  - HTML-formatted analytics summary
  - Top performer highlights
- Order management
- Category management

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.2.2
- **Database**: PostgreSQL 16 with Flyway migrations
- **Cache**: Redis for session management
- **Security**: Spring Security with OAuth2
- **Email**: Spring Mail with Thymeleaf templates
- **Payment**: Stripe integration
- **Testing**: JUnit 5, Testcontainers, Selenium, Mockito
- **Code Quality**: Checkstyle, JaCoCo coverage

## Quick Start

### Prerequisites

- Java 21
- Docker and Docker Compose
- Maven 3.8+

### Run the Application

```bash
# Start infrastructure (PostgreSQL, Redis, MailHog)
docker compose up -d

# Build and run
mvn clean install
mvn spring-boot:run
```

Application runs on `http://localhost:8080`

### Access Admin Dashboard

1. Login at `http://localhost:8080/login`
2. Navigate to `http://localhost:8080/admin/analytics`

## Analytics Feature

### View Tracking
- Automatic tracking when users view food item details
- Session-based deduplication prevents duplicate counts
- Real-time updates to analytics dashboard

### Order Tracking
- Event-driven tracking when orders complete
- Asynchronous processing with @Async
- Optimistic locking prevents race conditions

### Monthly Reports
- **Schedule**: 11 PM on last day of each month
- **Recipients**: All admin users
- **Content**: Analytics summary with view/order metrics
- **Testing**: View emails in MailHog at `http://localhost:8025`

### Performance
- Optimized queries with constructor expressions
- LEFT JOIN to include items with zero analytics
- Indexed columns for fast sorting
- @Timed metrics for monitoring

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn verify

# View coverage report
open target/site/jacoco/index.html
```

### Test Suites
- **Unit Tests**: Service logic with Mockito
- **MVC Tests**: Controller endpoints with MockMvc
- **Integration Tests**: Full stack with Testcontainers
- **E2E Tests**: Browser automation with Selenium

## Code Quality

```bash
# Checkstyle validation
mvn checkstyle:check

# JaCoCo coverage report
mvn jacoco:report
```

**Requirements**:
- 80%+ test coverage (Constitution Principle II)
- Google Java Style Guide compliance
- TDD workflow (Red-Green-Refactor)

## Architecture

### Analytics Components

```
Controller Layer
├── AdminAnalyticsController - Dashboard endpoint
├── FoodItemController - View tracking integration
└── OrderController - Order completion events

Service Layer
├── AnalyticsTrackingService - Increment counters (@Retryable)
├── AnalyticsDashboardService - Data aggregation
├── MonthlyReportService - Report generation
└── EmailService - Email delivery (@Async)

Data Layer
├── FoodAnalyticsRepository - Optimized queries
├── MonthlyReportRepository - Report tracking
└── UserRepository - Admin user lookup

Event Layer
├── OrderCompletedEvent - Domain event
└── AnalyticsEventListener - Async event handler

Scheduler
└── MonthlyReportScheduler - Cron job (0 0 23 L * ?)
```

### Database Schema

**food_analytics**
- `id` (PK)
- `food_item_id` (FK, unique)
- `view_count` (indexed)
- `order_count` (indexed)
- `version` (optimistic locking)

**monthly_reports**
- `id` (PK)
- `report_date` (unique, indexed)
- `status` (PENDING, GENERATING, SENT, FAILED)
- `total_items`, `total_views`, `total_orders`

## Configuration

### Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=foodshop
DB_USER=foodshop
DB_PASS=secret

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# OAuth2
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# Stripe
STRIPE_API_KEY=your-stripe-key

# Email
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_FROM=noreply@foodshop.com
```

### Profiles

- **dev**: Local development with MailHog
- **test**: Test environment with Testcontainers
- **prod**: Production with real SMTP

## Documentation

- [Specification](specs/003-food-analytics/spec.md) - Feature requirements
- [Technical Plan](specs/003-food-analytics/plan.md) - Architecture decisions
- [Quickstart Guide](specs/003-food-analytics/quickstart.md) - Setup instructions
- [API Contracts](specs/003-food-analytics/contracts/README.md) - Endpoint docs

## Contributing

1. Follow TDD workflow (tests first)
2. Maintain 80%+ coverage
3. Run Checkstyle before commit
4. Use conventional commits

## License

MIT License - See LICENSE file for details

## Support

For issues or questions, contact the development team or open a GitHub issue.
