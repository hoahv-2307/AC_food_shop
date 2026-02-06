# Tasks: Food Analytics Dashboard and Monthly Reporting

**Input**: Design documents from `/specs/003-food-analytics/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure - all setup complete, no tasks needed.

✅ **Status**: Complete - Project already initialized with Spring Boot 3.2.2, Java 21, PostgreSQL, Redis, Spring Security, Spring Mail, Flyway, Checkstyle, JUnit 5, Testcontainers.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core analytics infrastructure that MUST be complete before user stories

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database Schema

- [X] T001 [P] Create Flyway migration V007 for food_analytics table in src/main/resources/db/migration/V007__create_food_analytics_table.sql
- [X] T002 [P] Create Flyway migration V008 for monthly_reports table in src/main/resources/db/migration/V008__create_monthly_reports_table.sql

### Domain Entities

- [X] T003 [P] Create FoodAnalytics entity in src/main/java/com/foodshop/domain/FoodAnalytics.java
- [X] T004 [P] Create ReportStatus enum in src/main/java/com/foodshop/domain/ReportStatus.java
- [X] T005 [P] Create MonthlyReport entity in src/main/java/com/foodshop/domain/MonthlyReport.java

### DTOs

- [X] T006 [P] Create FoodAnalyticsDTO record in src/main/java/com/foodshop/dto/FoodAnalyticsDTO.java
- [X] T007 [P] Create MonthlyReportSummaryDTO record in src/main/java/com/foodshop/dto/MonthlyReportSummaryDTO.java

### Repositories

- [X] T008 [P] Create FoodAnalyticsRepository interface in src/main/java/com/foodshop/repository/FoodAnalyticsRepository.java
- [X] T009 [P] Create MonthlyReportRepository interface in src/main/java/com/foodshop/repository/MonthlyReportRepository.java

### Events

- [X] T010 [P] Create OrderCompletedEvent class in src/main/java/com/foodshop/event/OrderCompletedEvent.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - View Food Analytics Dashboard (Priority: P1) 🎯 MVP

**Goal**: Admin can view analytics dashboard displaying view and order counts for all food items

**Independent Test**: Login as admin, navigate to /admin/analytics, verify all food items show with view/order counts

### Tests for User Story 1 (TDD - Write FIRST) ✅

- [X] T011 [P] [US1] Unit test for AnalyticsTrackingService.incrementViewCount in src/test/java/com/foodshop/service/AnalyticsTrackingServiceTest.java
- [X] T012 [P] [US1] Unit test for AnalyticsTrackingService.incrementOrderCount in src/test/java/com/foodshop/service/AnalyticsTrackingServiceTest.java
- [X] T013 [P] [US1] Unit test for AnalyticsDashboardService.getAllAnalytics in src/test/java/com/foodshop/service/AnalyticsDashboardServiceTest.java
- [X] T014 [P] [US1] MVC test for AdminAnalyticsController.showDashboard in src/test/java/com/foodshop/controller/AdminAnalyticsControllerTest.java
- [X] T015 [P] [US1] Integration test for analytics dashboard with Testcontainers in src/test/java/com/foodshop/integration/AnalyticsDashboardIntegrationTest.java
- [X] T016 [US1] E2E test for analytics dashboard with Selenium in src/test/java/com/foodshop/e2e/AnalyticsDashboardE2ETest.java

### Core Services for User Story 1

- [X] T017 [US1] Implement AnalyticsTrackingService with view tracking logic in src/main/java/com/foodshop/service/AnalyticsTrackingService.java
- [X] T018 [US1] Implement AnalyticsDashboardService with dashboard data aggregation in src/main/java/com/foodshop/service/AnalyticsDashboardService.java

### Controller for User Story 1

- [X] T019 [US1] Implement AdminAnalyticsController with showDashboard endpoint in src/main/java/com/foodshop/controller/AdminAnalyticsController.java

### View Tracking Integration

- [X] T020 [US1] Add view tracking to FoodController.viewFoodDetail in src/main/java/com/foodshop/controller/FoodController.java
- [X] T021 [US1] Create AnalyticsEventListener with handleOrderCompleted in src/main/java/com/foodshop/event/AnalyticsEventListener.java

### Configuration

- [X] T022 [P] [US1] Create AsyncConfig for @Async task executor in src/main/java/com/foodshop/config/AsyncConfig.java
- [X] T023 [P] [US1] Add @Timed annotations for performance metrics in AnalyticsTrackingService

### UI Template

- [X] T024 [US1] Create analytics dashboard Thymeleaf template in src/main/resources/templates/admin/analytics.html
- [X] T025 [US1] Update admin navigation fragment to include analytics link in src/main/resources/templates/fragments/navigation.html
- [X] T026 [P] [US1] Add analytics dashboard CSS styles in src/main/resources/static/css/analytics.css

**Checkpoint**: User Story 1 complete - Admin can view analytics dashboard with view/order counts

---

## Phase 4: User Story 2 - Monthly Analytics Email Report (Priority: P2)

**Goal**: Automated monthly email report sent to all admins with analytics summary

**Independent Test**: Trigger monthly report generation, verify admin receives properly formatted email in MailHog

### Tests for User Story 2 (TDD - Write FIRST) ✅

- [X] T027 [P] [US2] Unit test for MonthlyReportService.generateAndSendReport in src/test/java/com/foodshop/service/MonthlyReportServiceTest.java
- [X] T028 [P] [US2] Unit test for EmailService.sendMonthlyAnalyticsReport in src/test/java/com/foodshop/service/EmailServiceTest.java
- [X] T029 [P] [US2] Integration test for monthly report generation in src/test/java/com/foodshop/integration/MonthlyReportIntegrationTest.java
- [X] T030 [US2] Integration test for scheduled monthly report execution in src/test/java/com/foodshop/integration/MonthlyReportSchedulerTest.java

### Core Services for User Story 2

- [X] T031 [US2] Implement MonthlyReportService with report generation logic in src/main/java/com/foodshop/service/MonthlyReportService.java
- [X] T032 [US2] Extend EmailService with sendMonthlyAnalyticsReport method in src/main/java/com/foodshop/service/EmailService.java

### Scheduling Configuration

- [X] T033 [US2] Create SchedulingConfig with @EnableScheduling in src/main/java/com/foodshop/config/SchedulingConfig.java
- [X] T034 [US2] Create MonthlyReportScheduler with cron job in src/main/java/com/foodshop/scheduler/MonthlyReportScheduler.java

### Email Template

- [X] T035 [P] [US2] Create monthly report email Thymeleaf template in src/main/resources/templates/email/monthly-analytics-report.html

**Checkpoint**: User Story 2 complete - Monthly reports automatically sent to admins

---

## Phase 5: User Story 3 - Sort and Filter Analytics Data (Priority: P3)

**Goal**: Admin can sort analytics dashboard by most/least viewed or ordered

**Independent Test**: Access dashboard, click sort options, verify items reorder correctly

### Tests for User Story 3 (TDD - Write FIRST) ✅

- [X] T036 [P] [US3] Unit test for AnalyticsDashboardService sorting methods in src/test/java/com/foodshop/service/AnalyticsDashboardServiceTest.java
- [X] T037 [P] [US3] MVC test for AdminAnalyticsController sort parameter handling in src/test/java/com/foodshop/controller/AdminAnalyticsControllerTest.java
- [X] T038 [US3] E2E test for dashboard sorting with Selenium in src/test/java/com/foodshop/e2e/AnalyticsSortingE2ETest.java

### Implementation for User Story 3

- [X] T039 [US3] Add sorting query methods to FoodAnalyticsRepository in src/main/java/com/foodshop/repository/FoodAnalyticsRepository.java
- [X] T040 [US3] Add sorting logic to AnalyticsDashboardService in src/main/java/com/foodshop/service/AnalyticsDashboardService.java
- [X] T041 [US3] Update AdminAnalyticsController to handle sort parameter in src/main/java/com/foodshop/controller/AdminAnalyticsController.java
- [X] T042 [US3] Update analytics.html template with sort buttons in src/main/resources/templates/admin/analytics.html

**Checkpoint**: All user stories complete - Full analytics feature functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T043 [P] Add logging with SLF4J to all analytics services
- [X] T044 [P] Add error handling for email delivery failures in MonthlyReportService
- [X] T045 [P] Add retry logic with @Retryable for OptimisticLockException in AnalyticsTrackingService
- [X] T046 [P] Run Checkstyle validation: mvn checkstyle:check
- [X] T047 [P] Verify test coverage meets 80% threshold: mvn verify jacoco:report
- [X] T048 [P] Performance test dashboard query with 1000 food items
- [X] T049 [P] Validate quickstart.md setup instructions
- [X] T050 Update main README.md with analytics feature documentation

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup
  ✅ Complete (existing infrastructure)
  ↓
Phase 2: Foundational
  Tasks: T001-T010
  ⚠️ BLOCKS all user stories
  ↓
Phase 3: User Story 1 (P1) - Dashboard
  Tests: T011-T016 (write FIRST, must FAIL)
  Implementation: T017-T026
  ↓ (Can proceed to US2 or US3 in any order)
Phase 4: User Story 2 (P2) - Email Reports
  Tests: T027-T030 (write FIRST, must FAIL)
  Implementation: T031-T035
  
Phase 5: User Story 3 (P3) - Sorting
  Tests: T036-T038 (write FIRST, must FAIL)
  Implementation: T039-T042
  ↓
Phase 6: Polish
  Tasks: T043-T050
```

### User Story Independence

- **US1 (Dashboard)**: Independent - No dependencies on other stories
- **US2 (Email Reports)**: Uses US1's AnalyticsDashboardService to gather data, but independently testable
- **US3 (Sorting)**: Extends US1's dashboard with sort parameter, but independently testable

### Within Each User Story (TDD Workflow)

1. **Write Tests FIRST** (must FAIL before implementation)
2. **Implement minimum code to make tests PASS**
3. **Refactor** while keeping tests green
4. **Verify coverage** meets 80% threshold

### Parallel Execution Opportunities

**Phase 2 (Foundational)**: All tasks T001-T010 can run in parallel

**Phase 3 (US1)**: 
- Tests T011-T015 can run in parallel (T016 after T015)
- Services T017-T018 can run in parallel after tests written
- Config T022-T023 can run in parallel
- UI T024-T026 can run in parallel

**Phase 4 (US2)**:
- Tests T027-T029 can run in parallel (T030 after T029)
- Services T031-T032 can run in parallel after tests written
- Config T033-T034 can run in parallel
- Template T035 can run in parallel

**Phase 5 (US3)**:
- Tests T036-T037 can run in parallel (T038 after T037)
- Implementation T039-T042 sequential (dependencies on query → service → controller → UI)

**Phase 6 (Polish)**: All tasks T043-T049 can run in parallel (T050 after others)

---

## Implementation Strategy

### MVP First (Suggested)

**Minimum Viable Product = User Story 1 ONLY**

To deliver value quickly, implement in this order:
1. **Phase 2 (Foundational)**: T001-T010 (required foundation)
2. **Phase 3 (US1 Dashboard)**: T011-T026 (MVP - immediate value)
3. Deploy to staging for validation
4. **Phase 4 (US2 Email)**: T027-T035 (nice-to-have automation)
5. **Phase 5 (US3 Sorting)**: T036-T042 (enhancement)
6. **Phase 6 (Polish)**: T043-T050 (final touches)

### Incremental Delivery

Each user story is a deployable increment:
- **After US1**: Admins can view analytics dashboard
- **After US2**: Admins get automated monthly reports
- **After US3**: Admins can efficiently analyze data with sorting

---

## Task Count Summary

- **Phase 1 (Setup)**: 0 tasks (already complete)
- **Phase 2 (Foundational)**: 10 tasks
- **Phase 3 (US1 - Dashboard)**: 16 tasks
- **Phase 4 (US2 - Email)**: 9 tasks
- **Phase 5 (US3 - Sorting)**: 7 tasks
- **Phase 6 (Polish)**: 8 tasks

**Total**: 50 tasks

**Parallel Opportunities**: 22 tasks marked [P] can execute in parallel within their phase

**Test Tasks**: 18 tasks (36% of total) - ensures TDD approach and 80%+ coverage

---

## Validation Checklist

Before marking feature complete:

- [ ] All 50 tasks completed with checkbox marked
- [ ] Test coverage ≥80% verified with `mvn verify jacoco:report`
- [ ] Checkstyle passes with `mvn checkstyle:check`
- [ ] All unit tests pass
- [ ] All integration tests pass with Testcontainers
- [ ] All E2E tests pass with Selenium
- [ ] Performance targets met:
  - [ ] Dashboard load <3s for 1000 items
  - [ ] View tracking overhead <50ms
  - [ ] Email generation <5 minutes
- [ ] Manual testing per quickstart.md completed
- [ ] All user stories independently testable
- [ ] Constitution compliance verified:
  - [ ] Code Quality (Checkstyle passing)
  - [ ] Testing Standards (80%+ coverage, TDD followed)
  - [ ] UX Consistency (follows Thymeleaf patterns)
  - [ ] Performance Requirements (metrics passing)
  - [ ] Review Process (PR checklist complete)
