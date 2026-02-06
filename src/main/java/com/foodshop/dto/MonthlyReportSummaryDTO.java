package com.foodshop.dto;

import java.time.YearMonth;
import java.util.List;

/**
 * DTO for monthly report email template context.
 * Contains aggregated analytics data for the reporting period.
 */
public record MonthlyReportSummaryDTO(
    YearMonth reportMonth,
    int totalItems,
    long totalViews,
    long totalOrders,
    List<FoodAnalyticsDTO> items
) {
}
