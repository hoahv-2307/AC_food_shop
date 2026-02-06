package com.foodshop.controller;

import com.foodshop.dto.FoodAnalyticsDTO;
import com.foodshop.service.AnalyticsDashboardService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for admin analytics dashboard.
 *
 * <p>Admin-only access to view food item analytics (view and order counts).
 */
@Controller
@RequestMapping("/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

  private final AnalyticsDashboardService dashboardService;

  public AdminAnalyticsController(AnalyticsDashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  /**
   * Displays the analytics dashboard with all food items and their metrics.
   *
   * @param sort optional sort parameter (views_asc, views_desc, orders_asc, orders_desc)
   * @param model the Spring MVC model
   * @return the analytics dashboard view
   */
  @GetMapping
  public String showDashboard(
      @RequestParam(name = "sort", required = false) String sort, Model model) {

    // Fetch analytics data with sorting
    List<FoodAnalyticsDTO> analyticsList = dashboardService.getAllAnalytics(sort);

    // Calculate totals
    Long totalViews = dashboardService.getTotalViews();
    Long totalOrders = dashboardService.getTotalOrders();

    // Add to model
    model.addAttribute("analyticsList", analyticsList);
    model.addAttribute("totalViews", totalViews);
    model.addAttribute("totalOrders", totalOrders);
    model.addAttribute("currentSort", sort != null ? sort : "views_desc");

    return "admin/analytics";
  }
}
