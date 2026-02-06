package com.foodshop.controller;

import com.foodshop.domain.FoodItem;
import com.foodshop.service.CategoryService;
import com.foodshop.service.FoodItemService;
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

  private final FoodItemService foodItemService;
  private final CategoryService categoryService;

  public FoodItemController(FoodItemService foodItemService, CategoryService categoryService) {
    this.foodItemService = foodItemService;
    this.categoryService = categoryService;
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
   * @param id the food item ID
   * @param model the model
   * @return the detail view
   */
  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model) {
    FoodItem foodItem = foodItemService.findById(id);

    model.addAttribute("foodItem", foodItem);
    model.addAttribute("title", foodItem.getName());

    return "food/detail";
  }
}
