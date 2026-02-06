package com.foodshop.repository;

import com.foodshop.domain.FoodAnalytics;
import com.foodshop.domain.FoodItem;
import com.foodshop.dto.FoodAnalyticsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FoodAnalytics entity.
 * Provides optimized queries for dashboard display and analytics aggregation.
 */
@Repository
public interface FoodAnalyticsRepository extends JpaRepository<FoodAnalytics, Long> {
    
    /**
     * Find analytics record by food item ID.
     *
     * @param foodItemId the food item ID
     * @return optional analytics record
     */
    Optional<FoodAnalytics> findByFoodItemId(Long foodItemId);
    
    /**
     * Find analytics record by food item.
     *
     * @param foodItem the food item
     * @return optional analytics record
     */
    Optional<FoodAnalytics> findByFoodItem(FoodItem foodItem);
    
    /**
     * Get all food analytics sorted by view count descending.
     * Uses JPA constructor expression to avoid loading full entities.
     *
     * @return list of analytics DTOs sorted by views (highest first)
     */
    @Query("SELECT new com.foodshop.dto.FoodAnalyticsDTO(" +
           "f.id, f.name, f.imageUrl, COALESCE(a.viewCount, 0), COALESCE(a.orderCount, 0)) " +
           "FROM FoodItem f LEFT JOIN f.analytics a " +
           "ORDER BY a.viewCount DESC NULLS LAST")
    List<FoodAnalyticsDTO> findAllFoodAnalyticsSortedByViews();
    
    /**
     * Get all food analytics sorted by view count ascending.
     *
     * @return list of analytics DTOs sorted by views (lowest first)
     */
    @Query("SELECT new com.foodshop.dto.FoodAnalyticsDTO(" +
           "f.id, f.name, f.imageUrl, COALESCE(a.viewCount, 0), COALESCE(a.orderCount, 0)) " +
           "FROM FoodItem f LEFT JOIN f.analytics a " +
           "ORDER BY a.viewCount ASC NULLS FIRST")
    List<FoodAnalyticsDTO> findAllFoodAnalyticsSortedByViewsAsc();
    
    /**
     * Get all food analytics sorted by order count descending.
     *
     * @return list of analytics DTOs sorted by orders (highest first)
     */
    @Query("SELECT new com.foodshop.dto.FoodAnalyticsDTO(" +
           "f.id, f.name, f.imageUrl, COALESCE(a.viewCount, 0), COALESCE(a.orderCount, 0)) " +
           "FROM FoodItem f LEFT JOIN f.analytics a " +
           "ORDER BY a.orderCount DESC NULLS LAST")
    List<FoodAnalyticsDTO> findAllFoodAnalyticsSortedByOrders();
    
    /**
     * Get all food analytics sorted by order count ascending.
     *
     * @return list of analytics DTOs sorted by orders (lowest first)
     */
    @Query("SELECT new com.foodshop.dto.FoodAnalyticsDTO(" +
           "f.id, f.name, f.imageUrl, COALESCE(a.viewCount, 0), COALESCE(a.orderCount, 0)) " +
           "FROM FoodItem f LEFT JOIN f.analytics a " +
           "ORDER BY a.orderCount ASC NULLS FIRST")
    List<FoodAnalyticsDTO> findAllFoodAnalyticsSortedByOrdersAsc();
    
    /**
     * Calculate total view count across all food items.
     *
     * @return sum of all view counts
     */
    @Query("SELECT COALESCE(SUM(a.viewCount), 0) FROM FoodAnalytics a")
    Long sumAllViewCounts();
    
    /**
     * Calculate total order count across all food items.
     *
     * @return sum of all order counts
     */
    @Query("SELECT COALESCE(SUM(a.orderCount), 0) FROM FoodAnalytics a")
    Long sumAllOrderCounts();
}
