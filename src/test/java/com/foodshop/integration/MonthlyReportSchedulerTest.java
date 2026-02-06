package com.foodshop.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.foodshop.BaseIntegrationTest;
import com.foodshop.domain.Category;
import com.foodshop.domain.FoodItem;
import com.foodshop.domain.User;
import com.foodshop.domain.User.UserRole;
import com.foodshop.repository.CategoryRepository;
import com.foodshop.repository.FoodAnalyticsRepository;
import com.foodshop.repository.FoodItemRepository;
import com.foodshop.repository.MonthlyReportRepository;
import com.foodshop.repository.UserRepository;
import com.foodshop.scheduler.MonthlyReportScheduler;
import com.foodshop.service.AnalyticsTrackingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

/**
 * Integration tests for scheduled monthly report execution.
 *
 * <p>Tests scheduler configuration and report generation trigger.
 */
@DisplayName("Monthly Report Scheduler Integration Tests")
class MonthlyReportSchedulerTest extends BaseIntegrationTest {

  @Autowired private MonthlyReportScheduler scheduler;

  @SpyBean private MonthlyReportRepository reportRepository;

  @Autowired private AnalyticsTrackingService trackingService;

  @Autowired private FoodItemRepository foodItemRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private FoodAnalyticsRepository analyticsRepository;

  @Autowired private UserRepository userRepository;

  private User adminUser;

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
    adminUser.setRole(UserRole.ADMIN);
    adminUser = userRepository.save(adminUser);

    // Create test category
    Category category = new Category();
    category.setName("Main Dishes");
    category.setDescription("Delicious main dishes");
    category.setActive(true);
    category.setDisplayOrder(1);
    category = categoryRepository.save(category);

    // Create test food item
    FoodItem pizza = new FoodItem();
    pizza.setName("Pizza");
    pizza.setDescription("Delicious pizza");
    pizza.setPrice(new BigDecimal("12.99"));
    pizza.setImageUrl("/images/pizza.jpg");
    pizza.setCategory(category);
    pizza.setAvailable(true);
    pizza = foodItemRepository.save(pizza);

    // Track some analytics
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementOrderCount(pizza.getId(), 2);
  }

  @Test
  @DisplayName("Scheduler should be configured and able to trigger report generation")
  void testSchedulerConfiguration() {
    // When - Manually trigger the scheduled method
    scheduler.generateMonthlyReport();

    // Then - Verify report was created
    long reportCount = reportRepository.count();
    assertTrue(reportCount > 0, "At least one report should be created");
  }

  @Test
  @DisplayName("Manual trigger should generate report for previous month")
  void testManualTriggerGeneratesReport() {
    // Given
    LocalDate previousMonth = YearMonth.now().minusMonths(1).atDay(1);

    // When
    scheduler.generateMonthlyReport();

    // Then
    assertTrue(
        reportRepository.findByReportDate(previousMonth).isPresent(),
        "Report should be generated for previous month");
  }

  @Test
  @DisplayName("Scheduler should not create duplicate reports on multiple executions")
  void testSchedulerDuplicatePrevention() {
    // When - Trigger scheduler multiple times
    scheduler.generateMonthlyReport();
    scheduler.generateMonthlyReport();
    scheduler.generateMonthlyReport();

    // Then - Only one report per month should exist
    YearMonth previousMonth = YearMonth.now().minusMonths(1);
    long reportCount =
        reportRepository.findAll().stream()
            .filter(r -> r.getReportDate().equals(previousMonth))
            .count();
    assertEquals(1, reportCount, "Should have exactly one report for the month");
  }
}
