package com.foodshop.repository;

import com.foodshop.domain.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Cart entity operations.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

  /**
   * Finds a cart by user ID with all cart items and food items eagerly loaded.
   *
   * @param userId the user ID
   * @return optional containing the cart if found
   */
  @EntityGraph(attributePaths = {"items", "items.foodItem", "items.foodItem.category"})
  @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
  Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

  /**
   * Finds a cart by user ID.
   *
   * @param userId the user ID
   * @return optional containing the cart if found
   */
  Optional<Cart> findByUserId(Long userId);
}
