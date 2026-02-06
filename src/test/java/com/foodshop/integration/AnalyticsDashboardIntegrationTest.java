package com.foodshop.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodshop.BaseIntegrationTest;
import com.foodshop.domain.Category;
import com.foodshop.domain.FoodAnalytics;
import com.foodshop.domain.FoodItem;
import com.foodshop.dto.FoodAnalyticsDTO;
import com.foodshop.repository.CategoryRepository;
import com.foodshop.repository.FoodAnalyticsRepository;
import com.foodshop.repository.FoodItemRepository;
import com.foodshop.service.AnalyticsDashboardService;
import com.foodshop.service.AnalyticsTrackingService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for analytics dashboard with Testcontainers.
 *
 * <p>Tests full flow from tracking to dashboard display using real database.
 */
@DisplayName("Analytics Dashboard Integration Tests")
class AnalyticsDashboardIntegrationTest extends BaseIntegrationTest {

  @Autowired private AnalyticsTrackingService trackingService;

  @Autowired private AnalyticsDashboardService dashboardService;

  @Autowired private FoodItemRepository foodItemRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private FoodAnalyticsRepository analyticsRepository;

  private FoodItem pizza;
  private FoodItem burger;

  @BeforeEach
  void setUp() {
    // Clean up
    analyticsRepository.deleteAll();
    foodItemRepository.deleteAll();
    categoryRepository.deleteAll();

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
  }

  @Test
  @DisplayName("Dashboard should show analytics after view and order tracking")
  void testFullAnalyticsFlow() {
    // Given - Track some views and orders
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementViewCount(burger.getId());

    trackingService.incrementOrderCount(pizza.getId(), 5);
    trackingService.incrementOrderCount(burger.getId(), 2);

    // When
    List<FoodAnalyticsDTO> analytics = dashboardService.getAllAnalytics(null);

    // Then
    assertNotNull(analytics);
    assertEquals(2, analytics.size());

    // Pizza should have 2 views and 5 orders
    FoodAnalyticsDTO pizzaAnalytics =
        analytics.stream()
            .filter(a -> a.foodItemName().equals("Pizza"))
            .findFirst()
            .orElse(null);
    assertNotNull(pizzaAnalytics);
    assertEquals(2L, pizzaAnalytics.viewCount());
    assertEquals(5L, pizzaAnalytics.orderCount());

    // Burger should have 1 view and 2 orders
    FoodAnalyticsDTO burgerAnalytics =
        analytics.stream()
            .filter(a -> a.foodItemName().equals("Burger"))
            .findFirst()
            .orElse(null);
    assertNotNull(burgerAnalytics);
    assertEquals(1L, burgerAnalytics.viewCount());
    assertEquals(2L, burgerAnalytics.orderCount());
  }

  @Test
  @DisplayName("Dashboard should return correct totals")
  void testDashboardTotals() {
    // Given
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementViewCount(burger.getId());
    trackingService.incrementOrderCount(pizza.getId(), 10);
    trackingService.incrementOrderCount(burger.getId(), 5);

    // When
    Long totalViews = dashboardService.getTotalViews();
    Long totalOrders = dashboardService.getTotalOrders();

    // Then
    assertEquals(3L, totalViews);
    assertEquals(15L, totalOrders);
  }

  @Test
  @DisplayName("Dashboard sorting by views should work correctly")
  void testDashboardSortingByViews() {
    // Given - Pizza has more views than Burger
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementViewCount(pizza.getId());
    trackingService.incrementViewCount(burger.getId());

    // When
    List<FoodAnalyticsDTO> sortedDesc = dashboardService.getAllAnalytics("views_desc");
    List<FoodAnalyticsDTO> sortedAsc = dashboardService.getAllAnalytics("views_asc");

    // Then
    assertEquals("Pizza", sortedDesc.get(0).foodItemName()); // Highest views first
    assertEquals("Burger", sortedAsc.get(0).foodItemName()); // Lowest views first
  }

  @Test
  @DisplayName("Dashboard sorting by orders should work correctly")
  void testDashboardSortingByOrders() {
    // Given - Pizza has more orders than Burger
    trackingService.incrementOrderCount(pizza.getId(), 20);
    trackingService.incrementOrderCount(burger.getId(), 5);

    // When
    List<FoodAnalyticsDTO> sortedDesc = dashboardService.getAllAnalytics("orders_desc");
    List<FoodAnalyticsDTO> sortedAsc = dashboardService.getAllAnalytics("orders_asc");

    // Then
    assertEquals("Pizza", sortedDesc.get(0).foodItemName()); // Highest orders first
    assertEquals("Burger", sortedAsc.get(0).foodItemName()); // Lowest orders first
  }

  @Test
  @DisplayName("Dashboard should show food items with zero analytics")
  void testDashboardWithZeroAnalytics() {
    // Given - No tracking done
    // When
    List<FoodAnalyticsDTO> analytics = dashboardService.getAllAnalytics(null);

    // Then - Should still show all food items with 0 counts
    assertNotNull(analytics);
    assertEquals(2, analytics.size());
    assertTrue(
        analytics.stream()
            .allMatch(a -> a.viewCount() == 0L && a.orderCount() == 0L));
  }

  @Test
  @DisplayName("Concurrent view tracking should handle optimistic locking")
  void testConcurrentViewTracking() throws InterruptedException {
    // Given
    Long foodItemId = pizza.getId();

    // When - Simulate concurrent increments
    Thread t1 = new Thread(() -> trackingService.incrementViewCount(foodItemId));
    Thread t2 = new Thread(() -> trackingService.incrementViewCount(foodItemId));
    Thread t3 = new Thread(() -> trackingService.incrementViewCount(foodItemId));

    t1.start();
    t2.start();
    t3.start();

    t1.join();
    t2.join();
    t3.join();

    // Then - All increments should succeed
    FoodAnalytics analytics = analyticsRepository.findByFoodItem(pizza).orElseThrow();
    assertEquals(3L, analytics.getViewCount());
  }
}
