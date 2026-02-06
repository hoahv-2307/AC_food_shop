package com.foodshop.service;

import com.foodshop.domain.Category;
import com.foodshop.exception.ResourceNotFoundException;
import com.foodshop.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing categories.
 */
@Service
@Transactional(readOnly = true)
public class CategoryService {

  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  /**
   * Finds all active categories ordered by display order.
   *
   * @return list of active categories
   */
  public List<Category> findAllActive() {
    return categoryRepository.findAllActiveOrderedByDisplayOrder();
  }

  /**
   * Finds all categories (including inactive).
   *
   * @return list of all categories
   */
  public List<Category> findAll() {
    return categoryRepository.findAll();
  }

  /**
   * Finds a category by ID.
   *
   * @param id the category ID
   * @return the category
   * @throws ResourceNotFoundException if category not found
   */
  public Category findById(Long id) {
    return categoryRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.forEntity("Category", id));
  }
}
