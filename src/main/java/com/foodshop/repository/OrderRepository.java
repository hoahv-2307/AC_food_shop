package com.foodshop.repository;

import com.foodshop.domain.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Order entity operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * Finds all orders for a specific user.
   *
   * @param userId the user ID
   * @param pageable pagination information
   * @return page of user orders
   */
  @EntityGraph(attributePaths = {"items", "items.foodItem"})
  @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
  Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

  /**
   * Finds an order by Stripe session ID.
   *
   * @param stripeSessionId the Stripe session ID
   * @return optional containing the order if found
   */
  Optional<Order> findByStripeSessionId(String stripeSessionId);

  /**
   * Finds all orders with a specific status.
   *
   * @param status the order status
   * @param pageable pagination information
   * @return page of orders with the given status
   */
  @EntityGraph(attributePaths = {"user", "items", "items.foodItem"})
  @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
  Page<Order> findByStatus(@Param("status") Order.OrderStatus status, Pageable pageable);
}
