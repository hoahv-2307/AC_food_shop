# Implementation Plan: Food Analytics Dashboard and Monthly Reporting

**Branch**: `003-food-analytics` | **Date**: February 6, 2026 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-food-analytics/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Implement an analytics system that tracks food item views and orders, displays cumulative statistics on an admin dashboard, and sends automated monthly email reports to administrators. The feature enables data-driven business decisions about inventory, pricing, and menu curation through real-time dashboard access and proactive monthly reporting.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.2.2, Spring Data JPA, Hibernate, Thymeleaf, Spring Security, Spring Mail  
**Storage**: PostgreSQL (via Flyway migrations), Redis (for session-based view tracking)  
**Testing**: JUnit 5, Spring Boot Test, Testcontainers (PostgreSQL), Spring Security Test  
**Target Platform**: Linux server (Docker container deployment)  
**Project Type**: Web application (monolithic Spring Boot MVC with Thymeleaf frontend)  
**Performance Goals**: Dashboard load < 3s for 1000 food items, view tracking overhead < 50ms, email generation < 5 minutes  
**Constraints**: p95 API response < 500ms, session-based view deduplication, non-blocking email delivery  
**Scale/Scope**: Supports existing food shop app with ~100-500 food items, multiple admin users, monthly reporting for all items

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Code Quality**: Checkstyle already configured (checkstyle.xml), will apply same standards to analytics code
- [x] **Testing Standards**: JUnit 5 + Testcontainers framework in place, will apply TDD for analytics services
- [x] **UX Consistency**: Analytics dashboard will follow existing Thymeleaf layout/navigation patterns, admin-only access via Spring Security
- [x] **Performance Requirements**: Will track metrics via Micrometer/Prometheus (already configured), dashboard query optimization, async email sending
- [x] **Review Process**: Standard PR process with constitution compliance verification will apply

*No violations identified. Feature aligns with constitution principles.*

## Project Structure

### Documentation (this feature)

```text
specs/003-food-analytics/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── README.md        # API contracts for analytics endpoints
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/foodshop/
├── domain/
│   ├── FoodAnalytics.java           # NEW: Entity for tracking view/order counts
│   └── MonthlyReport.java           # NEW: Entity for report generation tracking
├── repository/
│   ├── FoodAnalyticsRepository.java # NEW: JPA repository for analytics
│   └── MonthlyReportRepository.java # NEW: JPA repository for reports
├── service/
│   ├── AnalyticsTrackingService.java  # NEW: View/order tracking logic
│   ├── AnalyticsDashboardService.java # NEW: Dashboard data aggregation
│   ├── MonthlyReportService.java      # NEW: Report generation logic
│   └── EmailService.java              # EXISTING: Will extend for analytics emails
├── controller/
│   └── AdminAnalyticsController.java  # NEW: Admin dashboard controller
└── config/
    └── SchedulingConfig.java          # NEW: Monthly report scheduling

src/main/resources/
├── db/migration/
│   ├── V007__create_food_analytics_table.sql  # NEW: Analytics tracking table
│   └── V008__create_monthly_reports_table.sql # NEW: Report tracking table
└── templates/
    └── admin/
        └── analytics.html             # NEW: Analytics dashboard view

src/test/java/com/foodshop/
├── service/
│   ├── AnalyticsTrackingServiceTest.java   # NEW: Unit tests
│   ├── AnalyticsDashboardServiceTest.java  # NEW: Unit tests
│   └── MonthlyReportServiceTest.java       # NEW: Unit tests
├── controller/
│   └── AdminAnalyticsControllerTest.java   # NEW: MVC tests
├── integration/
│   └── AnalyticsIntegrationTest.java       # NEW: Integration tests with Testcontainers
└── e2e/
    └── AnalyticsDashboardE2ETest.java      # NEW: Selenium tests for dashboard
```

**Structure Decision**: Single monolithic web application structure maintained. Analytics features integrate naturally into existing Spring Boot MVC architecture with domain-driven organization (entities, repositories, services, controllers). Leverages existing infrastructure (database, email, security, scheduling) without requiring additional projects or services.

---

## Phase 1 Deliverables Complete

The following artifacts have been generated during Phase 1:

✅ **research.md**: Technical decisions for view tracking (session-based), order tracking (event-driven), analytics storage (separate entity), monthly scheduling (Spring @Scheduled), email format (HTML with Thymeleaf), query optimization (JPA projections), concurrent updates (optimistic locking), and initialization strategy (Flyway migration).

✅ **data-model.md**: Complete JPA entity definitions for `FoodAnalytics` and `MonthlyReport`, database schema with Flyway migrations, repository interfaces with optimized queries, DTOs for dashboard and email, and integration with existing `FoodItem` entity.

✅ **contracts/README.md**: API contracts for admin analytics dashboard (GET /admin/analytics with sorting), view tracking integration (GET /food/{id}), order tracking integration (POST /orders/{id}/complete), monthly report email template, and testing contracts.

✅ **quickstart.md**: Developer setup guide with Docker Compose, database verification, manual testing procedures for view/order tracking, monthly report testing with MailHog, development workflow with TDD, performance testing commands, troubleshooting section, and useful command cheat sheet.

✅ **Agent context updated**: GitHub Copilot context file updated with Java 21, Spring Boot 3.2.2, Spring Scheduling, and email templating patterns.

---

## Constitution Re-Check (Post-Phase 1 Design)

*Re-evaluating constitution compliance after detailed design phase:*

### Code Quality Standards
- ✅ **Readability**: Entity classes use clear naming (FoodAnalytics, MonthlyReport), service layer has single responsibilities
- ✅ **Consistent Style**: All code will follow existing Checkstyle configuration (checkstyle.xml)
- ✅ **Single Responsibility**: Each service has one purpose (tracking, dashboard, reporting)
- ✅ **Error Handling**: Email failures logged, optimistic lock conflicts retried, scheduler failures tracked in database
- ✅ **Documentation**: Complex analytics queries include inline comments explaining JOIN strategy and NULLS LAST

**Status**: ✅ PASS - Design maintains code quality standards

### Testing Standards
- ✅ **Unit Tests**: All services (AnalyticsTrackingService, AnalyticsDashboardService, MonthlyReportService) will have unit tests
- ✅ **Integration Tests**: Dashboard endpoint, view tracking, order tracking, email sending covered
- ✅ **Contract Tests**: API contracts documented in contracts/README.md for dashboard endpoints
- ✅ **E2E Tests**: Selenium tests for dashboard navigation and data display
- ✅ **Test Quality**: Testcontainers ensures isolated PostgreSQL for integration tests, mocks for unit tests

**Status**: ✅ PASS - Comprehensive test strategy planned

### UX Consistency
- ✅ **Accessibility**: Dashboard uses semantic HTML table, follows existing layout patterns
- ✅ **Responsive Design**: Dashboard inherits responsive layout from base template
- ✅ **Error Messages**: 403/404/500 errors redirect to existing user-friendly error pages
- ✅ **Loading States**: Dashboard query optimized for <3s load time (no loading indicator needed at this threshold)
- ✅ **Consistent Patterns**: Uses existing Thymeleaf fragments (navigation, footer), standard admin menu integration

**Status**: ✅ PASS - UX remains consistent with existing application

### Performance Requirements
- ✅ **API Response Times**: Dashboard query optimized with single JOIN, indexed sort columns (target: <3s, well under p95 <500ms for read operations)
- ✅ **Page Load Times**: Server-side rendered page, no heavy JavaScript (FCP/TTI/LCP not applicable but will be fast)
- ✅ **Bundle Size**: No additional JavaScript needed (pure server-side rendering)
- ✅ **Database Queries**: No N+1 queries (single JOIN query), pagination not needed for <1000 items
- ✅ **Monitoring**: Micrometer @Timed annotations planned for dashboard, view tracking, and report generation

**Status**: ✅ PASS - Performance targets achievable with proposed design

### Review Process
- ✅ **Pre-Merge Requirements**: Standard CI/CD pipeline applies (lint, test, build, coverage)
- ✅ **Code Review**: Will require 1 approval, constitution compliance verification in checklist
- ✅ **Documentation**: All Phase 1 documentation complete (spec.md, plan.md, research.md, data-model.md, contracts, quickstart)
- ✅ **Testing Evidence**: Test contracts defined, Testcontainers setup for integration tests

**Status**: ✅ PASS - Review process requirements met

---

## Final Constitution Verdict

**Overall Status**: ✅ **PASSED**

**Summary**: The food analytics feature design fully complies with all AC-Food-Shop constitution principles. No violations identified. No complexity tracking needed. The feature leverages existing infrastructure (Spring Boot, PostgreSQL, Redis, Spring Security, Spring Mail) without introducing new dependencies or architectural complexity. Performance requirements are achievable through query optimization and asynchronous processing. Testing strategy ensures 80%+ coverage target. UX consistency maintained through existing Thymeleaf patterns.

**Approval for Phase 2**: ✅ Proceed to `/speckit.tasks` command to generate task breakdown.

---

## Next Phase

Run `/speckit.tasks` to generate detailed task breakdown organized by user story priority (P1: Dashboard, P2: Email Reports, P3: Sorting).
