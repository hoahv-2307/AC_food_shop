<!--
SYNC IMPACT REPORT
==================
Version Change: N/A → 1.0.0
Change Type: Initial constitution creation
Principles Established:
  - I. Code Quality Standards (NEW)
  - II. Testing Standards (NEW)
  - III. User Experience Consistency (NEW)
  - IV. Performance Requirements (NEW)
Sections Added:
  - Core Principles (4 principles)
  - Quality Gates & Review Process
  - Development Workflow
  - Governance
Templates Status:
  ✅ plan-template.md - Constitution Check section aligns with new principles
  ✅ spec-template.md - Requirements section aligns with UX/performance principles
  ✅ tasks-template.md - Task categorization supports quality/testing/performance requirements
Follow-up Items:
  - None (initial creation complete)
Rationale: Initial constitution establishing foundational governance for AC-food-shop project
-->

# AC-Food-Shop Constitution

## Core Principles

### I. Code Quality Standards

All code MUST meet the following non-negotiable quality criteria before merge:

- **Readability First**: Code is written for humans, not machines. Clear naming, logical structure, and appropriate comments are mandatory. No clever one-liners that sacrifice clarity.
- **Consistent Style**: Automated linting and formatting tools MUST pass without warnings. Configuration MUST be committed to the repository.
- **Single Responsibility**: Each function, class, and module has ONE clear purpose. Violations require explicit justification in code review.
- **Error Handling**: Every external dependency call (APIs, database, file I/O) MUST have explicit error handling. No silent failures.
- **Documentation**: Public APIs, complex algorithms, and non-obvious business logic MUST include inline documentation explaining "why", not "what".

**Rationale**: Poor code quality compounds over time, creating technical debt that slows development and increases bugs. This principle ensures code remains maintainable as the team and codebase grow.

### II. Testing Standards

Test coverage and quality are NON-NEGOTIABLE. The following testing pyramid MUST be maintained:

- **Unit Tests**: REQUIRED for all business logic. Minimum 80% line coverage for new code. Tests MUST be written BEFORE implementation (TDD).
- **Integration Tests**: REQUIRED for all API endpoints and external service integrations. Must cover happy path and error scenarios.
- **Contract Tests**: REQUIRED for all public APIs. Breaking changes MUST be detected before deployment.
- **End-to-End Tests**: REQUIRED for critical user journeys identified in feature specs. Must run in CI/CD before production deployment.
- **Test Quality**: Tests must be deterministic, fast (<5 min for full suite), and isolated. No test should depend on another test's execution.

**Failing tests block all merges.** Green CI is a non-negotiable gate.

**Rationale**: Testing is our primary safety net against regressions. The testing pyramid ensures we catch issues early (unit tests) while maintaining confidence in system behavior (integration/E2E tests). TDD ensures testable, modular design.

### III. User Experience Consistency

User-facing features MUST provide a consistent, accessible, and predictable experience:

- **Accessibility**: WCAG 2.1 Level AA compliance is mandatory for all UI components. Keyboard navigation, screen reader support, and sufficient color contrast are required.
- **Responsive Design**: All interfaces MUST function correctly on mobile (320px), tablet (768px), and desktop (1920px+) viewports.
- **Error Messages**: User-facing errors MUST be actionable and written in plain language. No technical jargon or stack traces exposed to users.
- **Loading States**: Any operation taking >200ms MUST show a loading indicator. Operations >5s MUST show progress feedback.
- **Consistent Patterns**: UI patterns (buttons, forms, navigation) MUST follow the established design system. Deviations require UX team approval.

**Rationale**: Inconsistent UX creates cognitive load, reduces trust, and increases support burden. This principle ensures users develop accurate mental models and can efficiently accomplish their goals.

### IV. Performance Requirements

Performance is a feature, not an afterthought. The following SLAs MUST be met:

- **API Response Times**: 
  - p50 < 100ms for read operations
  - p95 < 500ms for read operations
  - p99 < 1000ms for all operations
- **Page Load Times**: 
  - First Contentful Paint (FCP) < 1.5s
  - Time to Interactive (TTI) < 3.5s
  - Largest Contentful Paint (LCP) < 2.5s
- **Bundle Size**: 
  - Initial JavaScript bundle < 200KB (gzipped)
  - Total page weight < 1MB for primary user journeys
- **Database Queries**: 
  - N+1 queries are prohibited
  - All queries accessing >1000 rows MUST use pagination
  - Maximum query execution time: 100ms
- **Monitoring**: All performance metrics MUST be tracked in production with alerting on SLA violations.

**Performance budgets are enforced in CI/CD.** Regressions beyond 10% block deployment.

**Rationale**: Performance directly impacts user satisfaction, conversion rates, and operational costs. These requirements ensure AC-food-shop remains fast and scalable as usage grows.

## Quality Gates & Review Process

### Pre-Merge Requirements

Every pull request MUST satisfy ALL of the following before merge approval:

1. **Automated Checks**:
   - All CI/CD pipeline stages pass (lint, test, build)
   - Test coverage meets minimum thresholds (80% for new code)
   - Performance budgets not exceeded
   - Security scans pass without high/critical vulnerabilities

2. **Code Review**:
   - Minimum 1 approval from a team member
   - All review comments addressed or explicitly deferred with justification
   - Constitution compliance verified (reviewer responsibility)

3. **Documentation**:
   - Feature specifications updated if user-facing behavior changed
   - API documentation updated for endpoint changes
   - Migration guides provided for breaking changes

4. **Testing Evidence**:
   - Manual testing checklist completed for UI changes
   - Integration test results linked in PR description
   - Performance benchmark results for performance-sensitive changes

### Review Responsibilities

Reviewers MUST verify:
- Code adheres to all Core Principles (I-IV)
- Tests adequately cover new functionality
- No obvious security vulnerabilities introduced
- Changes align with feature specification requirements

## Development Workflow

### Feature Development Lifecycle

1. **Specification Phase** (`/speckit.specify`):
   - User stories prioritized and acceptance criteria defined
   - Constitution compliance pre-checked
   - Technical feasibility validated

2. **Planning Phase** (`/speckit.plan`):
   - Technical approach researched and documented
   - Data models and API contracts designed
   - Architecture decisions recorded

3. **Task Breakdown** (`/speckit.tasks`):
   - Implementation tasks organized by user story
   - Dependencies identified and sequenced
   - Test tasks created BEFORE implementation tasks

4. **Implementation** (`/speckit.implement`):
   - TDD cycle: Write failing test → Implement feature → Refactor
   - Commit messages reference task IDs and user stories
   - Continuous integration provides fast feedback

5. **Review & Deployment**:
   - Code review per Quality Gates requirements
   - Staging deployment for manual validation
   - Production deployment with monitoring

### Branching Strategy

- **main**: Production-ready code only. Protected branch.
- **feature/###-name**: Feature development. Created from main, merged via PR.
- **hotfix/###-name**: Critical production fixes. Fast-tracked review process.

## Governance

### Constitutional Authority

This constitution supersedes all other development practices, guidelines, and conventions. In case of conflict, constitution principles take precedence.

### Amendment Process

Amendments require:
1. Written proposal documenting rationale and impact analysis
2. Team review and consensus (no blocking objections)
3. Migration plan for existing code if applicable
4. Version increment per semantic versioning:
   - MAJOR: Backward-incompatible principle changes
   - MINOR: New principles or section additions
   - PATCH: Clarifications or non-semantic refinements

### Compliance Enforcement

- All PRs MUST include constitution compliance verification in review checklist
- Violations require explicit justification and must be tracked as technical debt
- Repeated violations trigger team retrospective to address root causes
- Constitution compliance is a factor in code quality metrics

### Living Document

The constitution MUST be reviewed quarterly for relevance and effectiveness. Principles that no longer serve the project's goals should be amended or removed.

**Version**: 1.0.0 | **Ratified**: 2026-02-06 | **Last Amended**: 2026-02-06
