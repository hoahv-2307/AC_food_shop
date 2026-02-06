package com.foodshop.service;

import com.foodshop.domain.FoodAnalytics;
import com.foodshop.domain.FoodItem;
import com.foodshop.exception.ResourceNotFoundException;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for tracking food item analytics (view and order counts).
 *
 * <p>Uses optimistic locking with @Retryable to handle concurrent updates safely.
 */
@Service
public class AnalyticsTrackingService {

  private static final Logger logger = LoggerFactory.getLogger(AnalyticsTrackingService.class);

  private final FoodAnalyticsService foodAnalyticsService;
  private final FoodItemService foodItemService;

  public AnalyticsTrackingService(
      FoodAnalyticsService foodAnalyticsService, FoodItemService foodItemService) {
    this.foodAnalyticsService = foodAnalyticsService;
    this.foodItemService = foodItemService;
  }

  /**
   * Increments the view count for a food item.
   *
   * <p>If analytics record doesn't exist, creates a new one. Uses optimistic locking with retry on
   * conflict.
   *
   * @param foodItemId the ID of the food item
   * @throws ResourceNotFoundException if food item not found
   */
  @Timed(value = "analytics.view.increment", description = "Time taken to increment view count")
  @Transactional
  public void incrementViewCount(Long foodItemId) {
    logger.debug("Incrementing view count for food item: {}", foodItemId);

    FoodItem foodItem = foodItemService.findById(foodItemId);

    FoodAnalytics analytics =
        foodAnalyticsService
            .findByFoodItemId(foodItemId)
            .orElseGet(() -> foodAnalyticsService.createNewAnalytics(foodItem));

    analytics.setViewCount(analytics.getViewCount() + 1);
    foodAnalyticsService.save(analytics);

    logger.debug(
        "View count incremented for food item {}: {} views",
        foodItemId,
        analytics.getViewCount());
  }

  /**
   * Increments the order count for a food item by the specified quantity.
   *
   * <p>If analytics record doesn't exist, creates a new one. Uses optimistic locking with retry on
   * conflict.
   *
   * @param foodItemId the ID of the food item
   * @param quantity the quantity ordered
   * @throws ResourceNotFoundException if food item not found
   */
  @Timed(value = "analytics.order.increment", description = "Time taken to increment order count")
  @Transactional
  public void incrementOrderCount(Long foodItemId, int quantity) {
    logger.debug("Incrementing order count for food item: {} by {}", foodItemId, quantity);

    FoodItem foodItem = foodItemService.findById(foodItemId);

    FoodAnalytics analytics =
        foodAnalyticsService
            .findByFoodItemId(foodItemId)
            .orElseGet(() -> foodAnalyticsService.createNewAnalytics(foodItem));

    analytics.setOrderCount(analytics.getOrderCount() + quantity);
    foodAnalyticsService.save(analytics);

    logger.debug(
        "Order count incremented for food item {}: {} orders",
        foodItemId,
        analytics.getOrderCount());
  }

}
