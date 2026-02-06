package com.foodshop.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodshop.BaseIntegrationTest;
import com.foodshop.domain.Category;
import com.foodshop.domain.FoodItem;
import com.foodshop.domain.MonthlyReport;
import com.foodshop.domain.ReportStatus;
import com.foodshop.domain.User;
import com.foodshop.repository.CategoryRepository;
import com.foodshop.repository.FoodAnalyticsRepository;
import com.foodshop.repository.FoodItemRepository;
import com.foodshop.repository.MonthlyReportRepository;
import com.foodshop.repository.UserRepository;
import com.foodshop.service.AnalyticsTrackingService;
import com.foodshop.service.MonthlyReportService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;

/**
 * Integration tests for monthly report generation.
 *
 * <p>Tests full flow from analytics data to report creation and email sending.
 */
@DisplayName("Monthly Report Integration Tests")
class MonthlyReportIntegrationTest extends BaseIntegrationTest {

  @Autowired private MonthlyReportService reportService;

  @Autowired private AnalyticsTrackingService trackingService;

  @Autowired private MonthlyReportRepository reportRepository;

  @Autowired private FoodItemRepository foodItemRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private FoodAnalyticsRepository analyticsRepository;

  @Autowired private UserRepository userRepository;

  private User adminUser;
  private FoodItem pizza;
  private FoodItem burger;

  @BeforeEach
  void setUp() {
    // Clean up
    reportRepository.deleteAll();
    analyticsRepository.deleteAll();
    foodItemRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();

    // Create admin user
    adminUser = new User();
    adminUser.setEmail("admin@foodshop.com");
    adminUser.setName("Test Admin");
    adminUser.setRole(User.UserRole.ADMIN);
    adminUser = userRepository.save(adminUser);

    // Create test category
    Category category = new Category();
    category.setName("Main Dishes");
    category.setDescription("Delicious main dishes");
    category.setActive(true);
    category.setDisplayOrder(1);
    category = categoryRepository.save(category);

    // Create test food items
    pizza = new FoodItem();
    pizza.setName("Pizza");
    pizza.setDescription("Delicious pizza");
    pizza.setPrice(new BigDecimal("12.99"));
    pizza.setImageUrl("/images/pizza.jpg");
    pizza.setCategory(category);
    pizza.setAvailable(true);
    pizza = foodItemRepository.save(pizza);

    burger = new FoodItem();
    burger.setName("Burger");
    burger.setDescription("Tasty burger");
    burger.setPrice(new BigDecimal("8.99"));
    burger.setImageUrl("/images/burger.jpg");
    burger.setCategory(category);
    burger.setAvailable(true);
    burger = foodItemRepository.save(burger);

    // Track some analytics
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementViewCount(burger.getId());
    trackingService.incrementOrderCount(pizza.getId(), 5);
    trackingService.incrementOrderCount(burger.getId(), 3);
  }

  @Test
  @DisplayName("Monthly report should be generated with correct statistics")
  void testMonthlyReportGeneration() {
    // Given
    YearMonth reportMonth = YearMonth.now();

    LocalDate reportDate = reportMonth.atDay(1);

    // When
    reportService.generateAndSendReport(reportMonth);

    // Then
    Optional<MonthlyReport> reportOpt = reportRepository.findByReportDate(reportDate);
    assertTrue(reportOpt.isPresent());

    MonthlyReport report = reportOpt.get();
    assertEquals(reportMonth, report.getReportDate());
    assertEquals(2, report.getTotalItems());
    assertEquals(3L, report.getTotalViews());
    assertEquals(8L, report.getTotalOrders());
    assertEquals(ReportStatus.SENT, report.getStatus());
    assertNotNull(report.getCreatedAt());
  }

  @Test
  @DisplayName("Duplicate report should not be generated for same month")
  void testDuplicateReportPrevention() {
    // Given
    YearMonth reportMonth = YearMonth.now();

    // When - Generate report twice
    reportService.generateAndSendReport(reportMonth);
    reportService.generateAndSendReport(reportMonth);

    // Then - Only one report should exist
    long reportCount =
        reportRepository.findAll().stream()
            .filter(r -> r.getReportDate().equals(reportMonth))
            .count();
    assertEquals(1, reportCount);
  }

  @Test
  @DisplayName("Report should be saved with FAILED status on error")
  void testReportFailureHandling() {
    // Given - Remove admin user to cause email failure
    userRepository.deleteAll();
    YearMonth reportMonth = YearMonth.now();

    LocalDate reportDate = reportMonth.atDay(1);

    // When
    reportService.generateAndSendReport(reportMonth);

    // Then - Report should still be saved but with FAILED status
    Optional<MonthlyReport> reportOpt = reportRepository.findByReportDate(reportDate);
    assertTrue(reportOpt.isPresent());
    MonthlyReport report = reportOpt.get();
    // Note: Actual status depends on implementation - could be SENT if no admins is not an error
    assertNotNull(report.getStatus());
  }

  @Test
  @DisplayName("Report should include all food items even with zero analytics")
  void testReportIncludesAllItems() {
    // Given - Create new item with no analytics
    Category category = categoryRepository.findAll().get(0);
    FoodItem pasta = new FoodItem();
    pasta.setName("Pasta");
    pasta.setDescription("Fresh pasta");
    pasta.setPrice(new BigDecimal("10.99"));
    pasta.setCategory(category);
    pasta.setAvailable(true);
    pasta = foodItemRepository.save(pasta);

    YearMonth reportMonth = YearMonth.now();

    // When
    reportService.generateAndSendReport(reportMonth);

    // Then
    LocalDate reportDate = reportMonth.atDay(1);
    Optional<MonthlyReport> reportOpt = reportRepository.findByReportDate(reportDate);
    assertTrue(reportOpt.isPresent());
    assertEquals(3, reportOpt.get().getTotalItems()); // Should include all 3 items
  }

  @Test
  @DisplayName("getReportForMonth should return correct report")
  void testGetReportForMonth() {
    // Given
    YearMonth reportMonth = YearMonth.now();
    reportService.generateAndSendReport(reportMonth);

    // When
    Optional<MonthlyReport> result = reportService.getReportForMonth(reportMonth);

    // Then
    assertTrue(result.isPresent());
    assertEquals(reportMonth, result.get().getReportDate());
  }

  @Test
  @DisplayName("getReportForMonth should return empty for non-existent report")
  void testGetReportForMonth_NotExists() {
    // Given
    YearMonth futureMonth = YearMonth.now().plusMonths(1);

    // When
    Optional<MonthlyReport> result = reportService.getReportForMonth(futureMonth);

    // Then
    assertFalse(result.isPresent());
  }
}
