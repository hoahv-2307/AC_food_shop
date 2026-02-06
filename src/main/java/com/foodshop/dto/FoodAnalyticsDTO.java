package com.foodshop.dto;

/**
 * DTO for displaying food analytics data on the dashboard.
 * Contains food item details with their view and order counts.
 */
public record FoodAnalyticsDTO(
    Long foodItemId,
    String foodItemName,
    String imageUrl,
    Long viewCount,
    Long orderCount
) {
    /**
     * Compact constructor for validation and default values.
     */
    public FoodAnalyticsDTO {
        if (viewCount == null) {
            viewCount = 0L;
        }
        if (orderCount == null) {
            orderCount = 0L;
        }
    }
}
