package com.foodshop.repository;

import com.foodshop.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  /**
   * Finds all active categories ordered by display order.
   *
   * @return list of active categories
   */
  @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder ASC")
  List<Category> findAllActiveOrderedByDisplayOrder();

  /**
   * Finds a category by its name.
   *
   * @param name the category name
   * @return list containing the category if found
   */
  List<Category> findByName(String name);
}
