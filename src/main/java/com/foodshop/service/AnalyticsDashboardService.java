package com.foodshop.service;

import com.foodshop.dto.FoodAnalyticsDTO;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for aggregating and retrieving food analytics data for the dashboard.
 *
 * <p>Provides methods to get analytics with different sort orders and calculate totals.
 */
@Service
public class AnalyticsDashboardService {

  private static final Logger logger = LoggerFactory.getLogger(AnalyticsDashboardService.class);

  private final FoodAnalyticsService foodAnalyticsService;

  public AnalyticsDashboardService(FoodAnalyticsService foodAnalyticsService) {
    this.foodAnalyticsService = foodAnalyticsService;
  }

  /**
   * Retrieves all food analytics with optional sorting.
   *
   * <p>Supported sort values:
   *
   * <ul>
   *   <li>views_asc - Sort by view count ascending
   *   <li>views_desc - Sort by view count descending (default)
   *   <li>orders_asc - Sort by order count ascending
   *   <li>orders_desc - Sort by order count descending
   * </ul>
   *
   * @param sort the sort parameter (can be null)
   * @return list of food analytics DTOs
   */
  @Timed(value = "analytics.dashboard.fetch", description = "Time taken to fetch dashboard data")
  @Transactional(readOnly = true)
  public List<FoodAnalyticsDTO> getAllAnalytics(String sort) {
    logger.debug("Fetching all analytics with sort: {}", sort);

    if (sort == null || sort.isEmpty()) {
      sort = "views_desc"; // Default sort
    }

    List<FoodAnalyticsDTO> result;

    switch (sort.toLowerCase()) {
      case "views_asc":
        result = foodAnalyticsService.findAllAnalyticsSortedByViewsAsc();
        break;
      case "views_desc":
        result = foodAnalyticsService.findAllAnalyticsSortedByViewsDesc();
        break;
      case "orders_asc":
        result = foodAnalyticsService.findAllAnalyticsSortedByOrdersAsc();
        break;
      case "orders_desc":
        result = foodAnalyticsService.findAllAnalyticsSortedByOrdersDesc();
        break;
      default:
        logger.warn("Invalid sort parameter: {}, using default views_desc", sort);
        result = foodAnalyticsService.findAllAnalyticsSortedByViewsDesc();
        break;
    }

    logger.debug("Fetched {} analytics records", result.size());
    return result;
  }

  /**
   * Calculates the total view count across all food items.
   *
   * @return total view count
   */
  @Transactional(readOnly = true)
  public Long getTotalViews() {
    Long total = foodAnalyticsService.sumAllViewCounts();
    logger.debug("Total views: {}", total);
    return total;
  }

  /**
   * Calculates the total order count across all food items.
   *
   * @return total order count
   */
  @Transactional(readOnly = true)
  public Long getTotalOrders() {
    Long total = foodAnalyticsService.sumAllOrderCounts();
    logger.debug("Total orders: {}", total);
    return total;
  }
}
