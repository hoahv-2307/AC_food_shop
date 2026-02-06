package com.foodshop.service;

import com.foodshop.domain.FoodItem;
import com.foodshop.exception.ResourceNotFoundException;
import com.foodshop.repository.FoodItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing food items.
 */
@Service
@Transactional(readOnly = true)
public class FoodItemService {

  private final FoodItemRepository foodItemRepository;

  public FoodItemService(FoodItemRepository foodItemRepository) {
    this.foodItemRepository = foodItemRepository;
  }

  /**
   * Finds all available food items with pagination.
   *
   * @param pageable pagination information
   * @return page of food items
   */
  public Page<FoodItem> findAllAvailable(Pageable pageable) {
    return foodItemRepository.findAllAvailable(pageable);
  }

  /**
   * Finds food items by category with pagination.
   *
   * @param categoryId the category ID
   * @param pageable pagination information
   * @return page of food items
   */
  public Page<FoodItem> findByCategoryId(Long categoryId, Pageable pageable) {
    return foodItemRepository.findByCategoryIdAndAvailable(categoryId, pageable);
  }

  /**
   * Searches food items by name or description.
   *
   * @param searchTerm the search term
   * @param pageable pagination information
   * @return page of matching food items
   */
  public Page<FoodItem> search(String searchTerm, Pageable pageable) {
    return foodItemRepository.searchByNameOrDescription(searchTerm, pageable);
  }

  /**
   * Finds top rated food items.
   *
   * @param minRatingCount minimum number of ratings
   * @param pageable pagination information
   * @return page of top rated food items
   */
  public Page<FoodItem> findTopRated(Integer minRatingCount, Pageable pageable) {
    return foodItemRepository.findTopRated(minRatingCount, pageable);
  }

  /**
   * Finds a food item by ID.
   *
   * @param id the food item ID
   * @return the food item
   * @throws ResourceNotFoundException if food item not found
   */
  public FoodItem findById(Long id) {
    return foodItemRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.forEntity("FoodItem", id));
  }
}
