package com.foodshop.repository;

import com.foodshop.domain.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for FoodItem entity operations.
 */
@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

  /**
   * Finds all available food items with their categories.
   *
   * @param pageable pagination information
   * @return page of available food items
   */
  @EntityGraph(attributePaths = {"category"})
  @Query("SELECT f FROM FoodItem f WHERE f.available = true")
  Page<FoodItem> findAllAvailable(Pageable pageable);

  /**
   * Finds all food items in a specific category.
   *
   * @param categoryId the category ID
   * @param pageable pagination information
   * @return page of food items in the category
   */
  @EntityGraph(attributePaths = {"category"})
  @Query("SELECT f FROM FoodItem f WHERE f.category.id = :categoryId AND f.available = true")
  Page<FoodItem> findByCategoryIdAndAvailable(
      @Param("categoryId") Long categoryId, Pageable pageable);

  /**
   * Searches food items by name or description.
   *
   * @param searchTerm the search term
   * @param pageable pagination information
   * @return page of matching food items
   */
  @EntityGraph(attributePaths = {"category"})
  @Query("SELECT f FROM FoodItem f WHERE f.available = true "
      + "AND (LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
      + "OR LOWER(f.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  Page<FoodItem> searchByNameOrDescription(
      @Param("searchTerm") String searchTerm, Pageable pageable);

  /**
   * Finds top rated food items.
   *
   * @param minRatingCount minimum number of ratings required
   * @param pageable pagination information
   * @return page of top rated food items
   */
  @EntityGraph(attributePaths = {"category"})
  @Query("SELECT f FROM FoodItem f WHERE f.available = true "
      + "AND f.ratingCount >= :minRatingCount ORDER BY f.avgRating DESC")
  Page<FoodItem> findTopRated(
      @Param("minRatingCount") Integer minRatingCount, Pageable pageable);
}
