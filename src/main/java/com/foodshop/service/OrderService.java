package com.foodshop.service;

import com.foodshop.domain.Cart;
import com.foodshop.domain.Order;
import com.foodshop.domain.OrderItem;
import com.foodshop.domain.User;
import com.foodshop.exception.ResourceNotFoundException;
import com.foodshop.repository.OrderRepository;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing orders.
 */
@Service
@Transactional
public class OrderService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

  private final OrderRepository orderRepository;
  private final CartService cartService;
  private final PaymentService paymentService;
  private final EmailService emailService;

  public OrderService(
      OrderRepository orderRepository,
      CartService cartService,
      PaymentService paymentService,
      EmailService emailService) {
    this.orderRepository = orderRepository;
    this.cartService = cartService;
    this.paymentService = paymentService;
    this.emailService = emailService;
  }

  /**
   * Creates an order from the user's cart and initiates Stripe checkout.
   *
   * @param user the user
   * @return the Stripe checkout session
   */
  public Session createOrder(User user) {
    Cart cart = cartService.getCartWithItems(user.getId());

    if (cart.getItems().isEmpty()) {
      throw new IllegalStateException("Cannot create order from empty cart");
    }

    // Calculate total
    BigDecimal totalAmount = cart.getItems().stream()
        .map(item -> item.getFoodItem().getPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Create order
    Order order = new Order();
    order.setUser(user);
    order.setStatus(Order.OrderStatus.PENDING);
    order.setTotalAmount(totalAmount);

    // Add order items
    for (var cartItem : cart.getItems()) {
      OrderItem orderItem = new OrderItem();
      orderItem.setFoodItem(cartItem.getFoodItem());
      orderItem.setQuantity(cartItem.getQuantity());
      orderItem.setPrice(cartItem.getFoodItem().getPrice());
      order.addItem(orderItem);
    }

    order = orderRepository.save(order);
    LOGGER.info("Created order {} for user {}", order.getId(), user.getEmail());

    // Create Stripe checkout session
    Long amountInCents = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();
    Session session = paymentService.createCheckoutSession(
        order.getId(),
        amountInCents,
        user.getEmail()
    );

    // Update order with Stripe session ID
    order.setStripeSessionId(session.getId());
    orderRepository.save(order);

    // Clear cart
    cartService.clearCart(user.getId());

    return session;
  }

  /**
   * Updates order status after successful payment.
   *
   * @param orderId the order ID
   * @param status the new status
   * @return the updated order
   */
  public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
    Order order = findById(orderId);
    Order.OrderStatus oldStatus = order.getStatus();
    order.setStatus(status);
    order = orderRepository.save(order);

    LOGGER.info("Updated order {} status from {} to {}", orderId, oldStatus, status);

    // Send notifications based on status
    if (status == Order.OrderStatus.CONFIRMED) {
      emailService.sendOrderConfirmation(
          order.getUser().getEmail(),
          "ORD-" + order.getId(),
          order.getTotalAmount().toString()
      );
    } else if (status != Order.OrderStatus.PENDING) {
      emailService.sendOrderStatusUpdate(
          order.getUser().getEmail(),
          "ORD-" + order.getId(),
          status.name()
      );
    }

    return order;
  }

  /**
   * Confirms an order after successful Stripe payment.
   *
   * @param stripeSessionId the Stripe session ID
   * @return the confirmed order
   */
  public Order confirmOrder(String stripeSessionId) {
    Order order = orderRepository.findByStripeSessionId(stripeSessionId)
        .orElseThrow(() -> ResourceNotFoundException.forField(
            "Order", "stripeSessionId", stripeSessionId));

    return updateOrderStatus(order.getId(), Order.OrderStatus.CONFIRMED);
  }

  /**
   * Finds orders for a specific user.
   *
   * @param userId the user ID
   * @param pageable pagination information
   * @return page of user orders
   */
  @Transactional(readOnly = true)
  public Page<Order> findByUserId(Long userId, Pageable pageable) {
    return orderRepository.findByUserId(userId, pageable);
  }

  /**
   * Finds an order by ID.
   *
   * @param id the order ID
   * @return the order
   * @throws ResourceNotFoundException if order not found
   */
  @Transactional(readOnly = true)
  public Order findById(Long id) {
    return orderRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.forEntity("Order", id));
  }

  /**
   * Finds orders by status.
   *
   * @param status the order status
   * @param pageable pagination information
   * @return page of orders
   */
  @Transactional(readOnly = true)
  public Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable) {
    return orderRepository.findByStatus(status, pageable);
  }
}
