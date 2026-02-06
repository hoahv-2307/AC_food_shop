package com.foodshop.repository;

import com.foodshop.domain.Rating;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Rating entity operations.
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

  /**
   * Finds all ratings for a specific food item.
   *
   * @param foodItemId the food item ID
   * @param pageable pagination information
   * @return page of ratings
   */
  @EntityGraph(attributePaths = {"user"})
  @Query("SELECT r FROM Rating r WHERE r.foodItem.id = :foodItemId ORDER BY r.createdAt DESC")
  Page<Rating> findByFoodItemId(@Param("foodItemId") Long foodItemId, Pageable pageable);

  /**
   * Finds a rating by user and food item.
   *
   * @param userId the user ID
   * @param foodItemId the food item ID
   * @return optional containing the rating if found
   */
  Optional<Rating> findByUserIdAndFoodItemId(Long userId, Long foodItemId);

  /**
   * Checks if a user has rated a food item.
   *
   * @param userId the user ID
   * @param foodItemId the food item ID
   * @return true if rating exists, false otherwise
   */
  boolean existsByUserIdAndFoodItemId(Long userId, Long foodItemId);

  /**
   * Counts the number of ratings for a food item.
   *
   * @param foodItemId the food item ID
   * @return count of ratings
   */
  long countByFoodItemId(Long foodItemId);

  /**
   * Calculates the average rating for a food item.
   *
   * @param foodItemId the food item ID
   * @return average rating (1-5) or null if no ratings exist
   */
  @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.foodItem.id = :foodItemId")
  Double calculateAverageRating(@Param("foodItemId") Long foodItemId);
}
