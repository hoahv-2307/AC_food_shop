package com.foodshop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodshop.domain.FoodAnalytics;
import com.foodshop.domain.FoodItem;
import com.foodshop.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Unit tests for AnalyticsTrackingService.
 *
 * <p>Tests view and order count increment logic with optimistic locking retry behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsTrackingService Unit Tests")
class AnalyticsTrackingServiceTest {

  @Mock private FoodAnalyticsService foodAnalyticsService;

  @Mock private FoodItemService foodItemService;

  @InjectMocks private AnalyticsTrackingService trackingService;

  private FoodItem testFoodItem;
  private FoodAnalytics testAnalytics;

  @BeforeEach
  void setUp() {
    testFoodItem = new FoodItem();
    testFoodItem.setId(1L);
    testFoodItem.setName("Test Food");

    testAnalytics = new FoodAnalytics();
    testAnalytics.setId(1L);
    testAnalytics.setFoodItem(testFoodItem);
    testAnalytics.setViewCount(10L);
    testAnalytics.setOrderCount(5L);
    testAnalytics.setVersion(0L);
  }

  @Test
  @DisplayName("incrementViewCount should increment view count by 1")
  void testIncrementViewCount_Success() {
    // Given
    when(foodItemService.findById(1L)).thenReturn(testFoodItem);
    when(foodAnalyticsService.findByFoodItemId(1L))
        .thenReturn(Optional.of(testAnalytics));
    when(foodAnalyticsService.save(any(FoodAnalytics.class))).thenReturn(testAnalytics);

    // When
    trackingService.incrementViewCount(1L);

    // Then
    verify(foodAnalyticsService).save(testAnalytics);
    assertEquals(11L, testAnalytics.getViewCount());
    assertEquals(5L, testAnalytics.getOrderCount()); // Order count unchanged
  }

  @Test
  @DisplayName("incrementViewCount should create analytics record if not exists")
  void testIncrementViewCount_CreateNewRecord() {
    // Given
    when(foodItemService.findById(1L)).thenReturn(testFoodItem);
    when(foodAnalyticsService.findByFoodItemId(1L)).thenReturn(Optional.empty());
    when(foodAnalyticsService.createNewAnalytics(testFoodItem))
        .thenAnswer(invocation -> {
          FoodAnalytics analytics = new FoodAnalytics();
          analytics.setFoodItem(testFoodItem);
          analytics.setViewCount(0L);
          analytics.setOrderCount(0L);
          return analytics;
        });
    when(foodAnalyticsService.save(any(FoodAnalytics.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    trackingService.incrementViewCount(1L);

    // Then
    verify(foodAnalyticsService).save(any(FoodAnalytics.class));
  }

  @Test
  @DisplayName("incrementViewCount should throw exception if food item not found")
  void testIncrementViewCount_FoodItemNotFound() {
    // Given
    when(foodItemService.findById(999L)).thenThrow(new ResourceNotFoundException("Food item not found with id: 999"));

    // When & Then
    assertThrows(
        ResourceNotFoundException.class, () -> trackingService.incrementViewCount(999L));
  }

  @Test
  @DisplayName("incrementViewCount should retry on optimistic locking failure")
  void testIncrementViewCount_OptimisticLockRetry() {
    // Given
    when(foodItemService.findById(1L)).thenReturn(testFoodItem);
    when(foodAnalyticsService.findByFoodItemId(1L))
        .thenReturn(Optional.of(testAnalytics));
    when(foodAnalyticsService.save(any(FoodAnalytics.class)))
        .thenThrow(new ObjectOptimisticLockingFailureException(FoodAnalytics.class, 1L))
        .thenReturn(testAnalytics); // Succeed on second attempt

    // When
    trackingService.incrementViewCount(1L);

    // Then
    verify(foodAnalyticsService, times(2)).save(any(FoodAnalytics.class));
  }

  @Test
  @DisplayName("incrementOrderCount should increment order count by specified quantity")
  void testIncrementOrderCount_Success() {
    // Given
    int quantity = 3;
    when(foodItemService.findById(1L)).thenReturn(testFoodItem);
    when(foodAnalyticsService.findByFoodItemId(1L))
        .thenReturn(Optional.of(testAnalytics));
    when(foodAnalyticsService.save(any(FoodAnalytics.class))).thenReturn(testAnalytics);

    // When
    trackingService.incrementOrderCount(1L, quantity);

    // Then
    verify(foodAnalyticsService).save(testAnalytics);
    assertEquals(8L, testAnalytics.getOrderCount()); // 5 + 3
    assertEquals(10L, testAnalytics.getViewCount()); // View count unchanged
  }

  @Test
  @DisplayName("incrementOrderCount should create analytics record if not exists")
  void testIncrementOrderCount_CreateNewRecord() {
    // Given
    when(foodItemService.findById(1L)).thenReturn(testFoodItem);
    when(foodAnalyticsService.findByFoodItemId(1L)).thenReturn(Optional.empty());
    when(foodAnalyticsService.createNewAnalytics(testFoodItem))
        .thenAnswer(invocation -> {
          FoodAnalytics analytics = new FoodAnalytics();
          analytics.setFoodItem(testFoodItem);
          analytics.setViewCount(0L);
          analytics.setOrderCount(0L);
          return analytics;
        });
    when(foodAnalyticsService.save(any(FoodAnalytics.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    trackingService.incrementOrderCount(1L, 2);

    // Then
    verify(foodAnalyticsService).save(any(FoodAnalytics.class));
  }

  @Test
  @DisplayName("incrementOrderCount should throw exception if food item not found")
  void testIncrementOrderCount_FoodItemNotFound() {
    // Given
    when(foodItemService.findById(999L)).thenThrow(new ResourceNotFoundException("Food item not found with id: 999"));

    // When & Then
    assertThrows(
        ResourceNotFoundException.class, () -> trackingService.incrementOrderCount(999L, 1));
  }

  @Test
  @DisplayName("incrementOrderCount should retry on optimistic locking failure")
  void testIncrementOrderCount_OptimisticLockRetry() {
    // Given
    when(foodItemService.findById(1L)).thenReturn(testFoodItem);
    when(foodAnalyticsService.findByFoodItemId(1L))
        .thenReturn(Optional.of(testAnalytics));
    when(foodAnalyticsService.save(any(FoodAnalytics.class)))
        .thenThrow(new ObjectOptimisticLockingFailureException(FoodAnalytics.class, 1L))
        .thenReturn(testAnalytics); // Succeed on second attempt

    // When
    trackingService.incrementOrderCount(1L, 2);

    // Then
    verify(foodAnalyticsService, times(2)).save(any(FoodAnalytics.class));
  }
}
