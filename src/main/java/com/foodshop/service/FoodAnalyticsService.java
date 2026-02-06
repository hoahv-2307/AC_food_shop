package com.foodshop.service;

import com.foodshop.domain.FoodAnalytics;
import com.foodshop.domain.FoodItem;
import com.foodshop.dto.FoodAnalyticsDTO;
import com.foodshop.repository.FoodAnalyticsRepository;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing food analytics data.
 *
 * <p>This service acts as the only access point to FoodAnalyticsRepository, ensuring proper
 * separation of concerns.
 */
@Service
public class FoodAnalyticsService {

  private static final Logger logger = LoggerFactory.getLogger(FoodAnalyticsService.class);

  private final FoodAnalyticsRepository analyticsRepository;

  public FoodAnalyticsService(FoodAnalyticsRepository analyticsRepository) {
    this.analyticsRepository = analyticsRepository;
  }

  /**
   * Find analytics record by food item ID.
   *
   * @param foodItemId the food item ID
   * @return optional analytics record
   */
  @Transactional(readOnly = true)
  public Optional<FoodAnalytics> findByFoodItemId(Long foodItemId) {
    return analyticsRepository.findByFoodItemId(foodItemId);
  }

  /**
   * Save or update analytics record.
   *
   * @param analytics the analytics record to save
   * @return saved analytics record
   */
  @Transactional
  public FoodAnalytics save(FoodAnalytics analytics) {
    return analyticsRepository.save(analytics);
  }

  /**
   * Create a new FoodAnalytics record with initial counts of zero.
   *
   * @param foodItem the food item to create analytics for
   * @return new FoodAnalytics instance
   */
  public FoodAnalytics createNewAnalytics(FoodItem foodItem) {
    logger.debug("Creating new analytics record for food item: {}", foodItem.getId());
    FoodAnalytics analytics = new FoodAnalytics();
    analytics.setFoodItem(foodItem);
    analytics.setViewCount(0L);
    analytics.setOrderCount(0L);
    return analytics;
  }

  /**
   * Get all food analytics sorted by view count descending.
   *
   * @return list of analytics DTOs sorted by views (highest first)
   */
  @Timed(
      value = "analytics.query.views.desc",
      description = "Time taken to query analytics sorted by views desc")
  @Transactional(readOnly = true)
  public List<FoodAnalyticsDTO> findAllAnalyticsSortedByViewsDesc() {
    return analyticsRepository.findAllFoodAnalyticsSortedByViews();
  }

  /**
   * Get all food analytics sorted by view count ascending.
   *
   * @return list of analytics DTOs sorted by views (lowest first)
   */
  @Timed(
      value = "analytics.query.views.asc",
      description = "Time taken to query analytics sorted by views asc")
  @Transactional(readOnly = true)
  public List<FoodAnalyticsDTO> findAllAnalyticsSortedByViewsAsc() {
    return analyticsRepository.findAllFoodAnalyticsSortedByViewsAsc();
  }

  /**
   * Get all food analytics sorted by order count descending.
   *
   * @return list of analytics DTOs sorted by orders (highest first)
   */
  @Timed(
      value = "analytics.query.orders.desc",
      description = "Time taken to query analytics sorted by orders desc")
  @Transactional(readOnly = true)
  public List<FoodAnalyticsDTO> findAllAnalyticsSortedByOrdersDesc() {
    return analyticsRepository.findAllFoodAnalyticsSortedByOrders();
  }

  /**
   * Get all food analytics sorted by order count ascending.
   *
   * @return list of analytics DTOs sorted by orders (lowest first)
   */
  @Timed(
      value = "analytics.query.orders.asc",
      description = "Time taken to query analytics sorted by orders asc")
  @Transactional(readOnly = true)
  public List<FoodAnalyticsDTO> findAllAnalyticsSortedByOrdersAsc() {
    return analyticsRepository.findAllFoodAnalyticsSortedByOrdersAsc();
  }

  /**
   * Calculate total view count across all food items.
   *
   * @return sum of all view counts
   */
  @Transactional(readOnly = true)
  public Long sumAllViewCounts() {
    Long total = analyticsRepository.sumAllViewCounts();
    return total != null ? total : 0L;
  }

  /**
   * Calculate total order count across all food items.
   *
   * @return sum of all order counts
   */
  @Transactional(readOnly = true)
  public Long sumAllOrderCounts() {
    Long total = analyticsRepository.sumAllOrderCounts();
    return total != null ? total : 0L;
  }

  /**
   * Find analytics record by food item.
   *
   * @param foodItem the food item
   * @return optional analytics record
   */
  @Transactional(readOnly = true)
  public Optional<FoodAnalytics> findByFoodItem(FoodItem foodItem) {
    return analyticsRepository.findByFoodItemId(foodItem.getId());
  }
}
