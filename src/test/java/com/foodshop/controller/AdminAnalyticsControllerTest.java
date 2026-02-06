package com.foodshop.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.foodshop.dto.FoodAnalyticsDTO;
import com.foodshop.service.AnalyticsDashboardService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MVC tests for AdminAnalyticsController.
 *
 * <p>Tests controller endpoints with security and model attributes.
 */
@WebMvcTest(AdminAnalyticsController.class)
@DisplayName("AdminAnalyticsController MVC Tests")
class AdminAnalyticsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AnalyticsDashboardService dashboardService;

  @Test
  @DisplayName("GET /admin/analytics should return 401 for unauthenticated user")
  void testShowDashboard_Unauthenticated() throws Exception {
    mockMvc.perform(get("/admin/analytics")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /admin/analytics should return 403 for non-admin user")
  @WithMockUser(roles = "USER")
  void testShowDashboard_NonAdmin() throws Exception {
    mockMvc.perform(get("/admin/analytics").with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /admin/analytics should return analytics dashboard for admin")
  @WithMockUser(roles = "ADMIN")
  void testShowDashboard_Success() throws Exception {
    // Given
    List<FoodAnalyticsDTO> analyticsData =
        List.of(
            new FoodAnalyticsDTO(1L, "Pizza", "/images/pizza.jpg", 100L, 50L),
            new FoodAnalyticsDTO(2L, "Burger", "/images/burger.jpg", 80L, 40L));

    when(dashboardService.getAllAnalytics(null)).thenReturn(analyticsData);
    when(dashboardService.getTotalViews()).thenReturn(180L);
    when(dashboardService.getTotalOrders()).thenReturn(90L);

    // When & Then
    mockMvc
        .perform(get("/admin/analytics").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/analytics"))
        .andExpect(model().attributeExists("analyticsList"))
        .andExpect(model().attribute("analyticsList", analyticsData))
        .andExpect(model().attribute("totalViews", 180L))
        .andExpect(model().attribute("totalOrders", 90L))
        .andExpect(model().attribute("currentSort", "views_desc"));
  }

  @Test
  @DisplayName("GET /admin/analytics with sort parameter should pass sort to service")
  @WithMockUser(roles = "ADMIN")
  void testShowDashboard_WithSortParameter() throws Exception {
    // Given
    List<FoodAnalyticsDTO> sortedData =
        List.of(new FoodAnalyticsDTO(2L, "Burger", "/images/burger.jpg", 80L, 40L));

    when(dashboardService.getAllAnalytics("orders_desc")).thenReturn(sortedData);
    when(dashboardService.getTotalViews()).thenReturn(80L);
    when(dashboardService.getTotalOrders()).thenReturn(40L);

    // When & Then
    mockMvc
        .perform(get("/admin/analytics").param("sort", "orders_desc").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/analytics"))
        .andExpect(model().attribute("currentSort", "orders_desc"))
        .andExpect(model().attribute("analyticsList", sortedData));
  }

  @Test
  @DisplayName("GET /admin/analytics should handle empty analytics list")
  @WithMockUser(roles = "ADMIN")
  void testShowDashboard_EmptyList() throws Exception {
    // Given
    when(dashboardService.getAllAnalytics(null)).thenReturn(List.of());
    when(dashboardService.getTotalViews()).thenReturn(0L);
    when(dashboardService.getTotalOrders()).thenReturn(0L);

    // When & Then
    mockMvc
        .perform(get("/admin/analytics").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/analytics"))
        .andExpect(model().attribute("analyticsList", List.of()))
        .andExpect(model().attribute("totalViews", 0L))
        .andExpect(model().attribute("totalOrders", 0L));
  }
}
