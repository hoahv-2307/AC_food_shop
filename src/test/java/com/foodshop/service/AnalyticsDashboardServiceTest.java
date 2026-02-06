package com.foodshop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodshop.dto.FoodAnalyticsDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for AnalyticsDashboardService.
 *
 * <p>Tests dashboard data aggregation with different sort orders.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsDashboardService Unit Tests")
class AnalyticsDashboardServiceTest {

  @Mock private FoodAnalyticsService foodAnalyticsService;

  @InjectMocks private AnalyticsDashboardService dashboardService;

  private List<FoodAnalyticsDTO> testAnalyticsList;

  @BeforeEach
  void setUp() {
    testAnalyticsList =
        List.of(
            new FoodAnalyticsDTO(1L, "Pizza", "/images/pizza.jpg", 100L, 50L),
            new FoodAnalyticsDTO(2L, "Burger", "/images/burger.jpg", 80L, 40L),
            new FoodAnalyticsDTO(3L, "Pasta", "/images/pasta.jpg", 60L, 30L));
  }

  @Test
  @DisplayName("getAllAnalytics with no sort should return default order (views desc)")
  void testGetAllAnalytics_NoSort() {
    // Given
    when(foodAnalyticsService.findAllAnalyticsSortedByViewsDesc()).thenReturn(testAnalyticsList);

    // When
    List<FoodAnalyticsDTO> result = dashboardService.getAllAnalytics(null);

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("Pizza", result.get(0).foodItemName());
    verify(foodAnalyticsService).findAllAnalyticsSortedByViewsDesc();
  }

  @Test
  @DisplayName("getAllAnalytics with sort=views_asc should return views ascending")
  void testGetAllAnalytics_ViewsAsc() {
    // Given
    List<FoodAnalyticsDTO> reversedList = List.copyOf(testAnalyticsList).reversed();
    when(foodAnalyticsService.findAllAnalyticsSortedByViewsAsc()).thenReturn(reversedList);

    // When
    List<FoodAnalyticsDTO> result = dashboardService.getAllAnalytics("views_asc");

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("Pasta", result.get(0).foodItemName());
    verify(foodAnalyticsService).findAllAnalyticsSortedByViewsAsc();
  }

  @Test
  @DisplayName("getAllAnalytics with sort=views_desc should return views descending")
  void testGetAllAnalytics_ViewsDesc() {
    // Given
    when(foodAnalyticsService.findAllAnalyticsSortedByViewsDesc()).thenReturn(testAnalyticsList);

    // When
    List<FoodAnalyticsDTO> result = dashboardService.getAllAnalytics("views_desc");

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("Pizza", result.get(0).foodItemName());
    verify(foodAnalyticsService).findAllAnalyticsSortedByViewsDesc();
  }

  @Test
  @DisplayName("getAllAnalytics with sort=orders_asc should return orders ascending")
  void testGetAllAnalytics_OrdersAsc() {
    // Given
    List<FoodAnalyticsDTO> reversedList = List.copyOf(testAnalyticsList).reversed();
    when(foodAnalyticsService.findAllAnalyticsSortedByOrdersAsc()).thenReturn(reversedList);

    // When
    List<FoodAnalyticsDTO> result = dashboardService.getAllAnalytics("orders_asc");

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("Pasta", result.get(0).foodItemName());
    verify(foodAnalyticsService).findAllAnalyticsSortedByOrdersAsc();
  }

  @Test
  @DisplayName("getAllAnalytics with sort=orders_desc should return orders descending")
  void testGetAllAnalytics_OrdersDesc() {
    // Given
    when(foodAnalyticsService.findAllAnalyticsSortedByOrdersDesc()).thenReturn(testAnalyticsList);

    // When
    List<FoodAnalyticsDTO> result = dashboardService.getAllAnalytics("orders_desc");

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("Pizza", result.get(0).foodItemName());
    verify(foodAnalyticsService).findAllAnalyticsSortedByOrdersDesc();
  }

  @Test
  @DisplayName("getAllAnalytics with invalid sort should default to views desc")
  void testGetAllAnalytics_InvalidSort() {
    // Given
    when(foodAnalyticsService.findAllAnalyticsSortedByViewsDesc()).thenReturn(testAnalyticsList);

    // When
    List<FoodAnalyticsDTO> result = dashboardService.getAllAnalytics("invalid_sort");

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    verify(foodAnalyticsService).findAllAnalyticsSortedByViewsDesc();
  }

  @Test
  @DisplayName("getTotalViews should sum all view counts")
  void testGetTotalViews() {
    // Given
    when(foodAnalyticsService.sumAllViewCounts()).thenReturn(240L);

    // When
    Long totalViews = dashboardService.getTotalViews();

    // Then
    assertEquals(240L, totalViews);
    verify(foodAnalyticsService).sumAllViewCounts();
  }

  @Test
  @DisplayName("getTotalOrders should sum all order counts")
  void testGetTotalOrders() {
    // Given
    when(foodAnalyticsService.sumAllOrderCounts()).thenReturn(120L);

    // When
    Long totalOrders = dashboardService.getTotalOrders();

    // Then
    assertEquals(120L, totalOrders);
    verify(foodAnalyticsService).sumAllOrderCounts();
  }
}
