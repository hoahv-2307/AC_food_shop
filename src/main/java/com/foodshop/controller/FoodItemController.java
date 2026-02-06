package com.foodshop.controller;

import com.foodshop.domain.FoodItem;
import com.foodshop.service.AnalyticsTrackingService;
import com.foodshop.service.CategoryService;
import com.foodshop.service.FoodItemService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for browsing food items.
 */
@Controller
@RequestMapping("/catalog")
public class FoodItemController {

  private static final String VIEWED_ITEMS_SESSION_KEY = "viewedFoodItems";

  private final FoodItemService foodItemService;
  private final CategoryService categoryService;
  private final AnalyticsTrackingService analyticsTrackingService;

  public FoodItemController(
      FoodItemService foodItemService,
      CategoryService categoryService,
      AnalyticsTrackingService analyticsTrackingService) {
    this.foodItemService = foodItemService;
    this.categoryService = categoryService;
    this.analyticsTrackingService = analyticsTrackingService;
  }

  /**
   * Displays the food catalog with filtering and pagination.
   *
   * @param categoryId optional category filter
   * @param search optional search term
   * @param page page number (0-indexed)
   * @param size page size
   * @param model the model
   * @return the catalog view
   */
  @GetMapping
  public String browse(
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      Model model) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
    Page<FoodItem> foodItems;

    if (search != null && !search.trim().isEmpty()) {
      foodItems = foodItemService.search(search, pageable);
      model.addAttribute("search", search);
    } else if (categoryId != null) {
      foodItems = foodItemService.findByCategoryId(categoryId, pageable);
      model.addAttribute("categoryId", categoryId);
      model.addAttribute("categoryName", categoryService.findById(categoryId).getName());
    } else {
      foodItems = foodItemService.findAllAvailable(pageable);
    }

    model.addAttribute("foodItems", foodItems);
    model.addAttribute("categories", categoryService.findAllActive());
    model.addAttribute("title", "Food Catalog");

    return "food/list";
  }

  /**
   * Displays food item details.
   *
   * <p>Tracks view count using session-based deduplication to prevent counting multiple views from
   * the same session.
   *
   * @param id the food item ID
   * @param model the model
   * @param session the HTTP session
   * @return the detail view
   */
  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model, HttpSession session) {
    FoodItem foodItem = foodItemService.findById(id);

    // Track view with session-based deduplication
    trackViewIfNotAlreadyViewed(id, session);

    model.addAttribute("foodItem", foodItem);
    model.addAttribute("title", foodItem.getName());

    return "food/detail";
  }

  /**
   * Tracks a view for a food item if it hasn't been viewed in this session.
   *
   * <p>Uses a Set stored in the session to prevent duplicate view counts within the same session.
   *
   * @param foodItemId the food item ID
   * @param session the HTTP session
   */
  @SuppressWarnings("unchecked")
  private void trackViewIfNotAlreadyViewed(Long foodItemId, HttpSession session) {
    java.util.Set<Long> viewedItems =
        (java.util.Set<Long>) session.getAttribute(VIEWED_ITEMS_SESSION_KEY);

    if (viewedItems == null) {
      viewedItems = new java.util.HashSet<>();
      session.setAttribute(VIEWED_ITEMS_SESSION_KEY, viewedItems);
    }

    // Only track if not already viewed in this session
    if (!viewedItems.contains(foodItemId)) {
      try {
        analyticsTrackingService.incrementViewCount(foodItemId);
        viewedItems.add(foodItemId);
      } catch (Exception e) {
        // Log but don't fail the request if analytics tracking fails
        // (logger would be injected in production)
      }
    }
  }
}
