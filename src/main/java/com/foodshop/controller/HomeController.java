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

/**
 * Controller for the home page.
 */
@Controller
public class HomeController {

  private final FoodItemService foodItemService;
  private final CategoryService categoryService;

  public HomeController(FoodItemService foodItemService, CategoryService categoryService) {
    this.foodItemService = foodItemService;
    this.categoryService = categoryService;
  }

  /**
   * Displays the home page with featured items.
   *
   * @param model the model
   * @return the home view
   */
  @GetMapping("/")
  public String index(Model model) {
    // Get featured items (top rated with at least 5 ratings)
    Pageable pageable = PageRequest.of(0, 8, Sort.by("avgRating").descending());
    Page<FoodItem> featuredItems = foodItemService.findTopRated(5, pageable);

    model.addAttribute("featuredItems", featuredItems.getContent());
    model.addAttribute("categories", categoryService.findAllActive());
    model.addAttribute("title", "Welcome");

    return "index";
  }
}
