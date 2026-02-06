package com.foodshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.foodshop.domain.*;
import com.foodshop.exception.ResourceNotFoundException;
import com.foodshop.repository.OrderRepository;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for OrderService.
 *
 * <p>Tests verify order lifecycle management including creation from cart, status updates, Stripe
 * integration, and email notifications.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private CartService cartService;

  @Mock private PaymentService paymentService;

  @Mock private EmailService emailService;

  @InjectMocks private OrderService orderService;

  private User testUser;
  private Cart testCart;
  private Order testOrder;
  private FoodItem testFoodItem;
  private CartItem testCartItem;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");

    Category category = new Category();
    category.setId(1L);
    category.setName("Pizza");

    testFoodItem = new FoodItem();
    testFoodItem.setId(1L);
    testFoodItem.setName("Margherita Pizza");
    testFoodItem.setPrice(new BigDecimal("12.99"));
    testFoodItem.setAvailable(true);
    testFoodItem.setCategory(category);

    testCart = new Cart();
    testCart.setId(1L);
    testCart.setUser(testUser);
    testCart.setItems(new ArrayList<>());

    testCartItem = new CartItem();
    testCartItem.setId(1L);
    testCartItem.setCart(testCart);
    testCartItem.setFoodItem(testFoodItem);
    testCartItem.setQuantity(2);
    testCart.getItems().add(testCartItem);

    testOrder = new Order();
    testOrder.setId(1L);
    testOrder.setUser(testUser);
    testOrder.setStatus(Order.OrderStatus.PENDING);
    testOrder.setTotalAmount(new BigDecimal("25.98"));
    testOrder.setItems(new ArrayList<>());

    pageable = PageRequest.of(0, 10);
  }

  @Test
  void createOrder_shouldCreateOrderFromCart() throws Exception {
    // Arrange
    when(cartService.getCartWithItems(testUser.getId())).thenReturn(testCart);
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    Session mockSession = mock(Session.class);
    when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/session123");
    when(mockSession.getId()).thenReturn("cs_test_123");
    when(paymentService.createCheckoutSession(anyLong(), anyLong(), anyString()))
        .thenReturn(mockSession);

    doNothing().when(cartService).clearCart(testUser.getId());

    // Act
    Session result = orderService.createOrder(testUser);

    // Assert
    assertNotNull(result);
    verify(cartService).getCartWithItems(testUser.getId());
    verify(orderRepository, times(2)).save(any(Order.class)); // Once for creation, once for Stripe session ID
    verify(paymentService).createCheckoutSession(anyLong(), anyLong(), eq(testUser.getEmail()));
    verify(cartService).clearCart(testUser.getId());
  }

  @Test
  void createOrder_shouldThrowException_whenCartIsEmpty() {
    // Arrange
    testCart.getItems().clear();
    when(cartService.getCartWithItems(testUser.getId())).thenReturn(testCart);

    // Act & Assert
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> orderService.createOrder(testUser));

    assertTrue(exception.getMessage().contains("empty"));
    verify(orderRepository, never()).save(any(Order.class));
    verify(paymentService, never()).createCheckoutSession(anyLong(), anyLong(), anyString());
  }

  @Test
  void createOrder_shouldCalculateTotalAmount() throws Exception {
    // Arrange
    CartItem item2 = new CartItem();
    item2.setFoodItem(testFoodItem);
    item2.setQuantity(1);
    testCart.getItems().add(item2);

    when(cartService.getCartWithItems(testUser.getId())).thenReturn(testCart);
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order order = invocation.getArgument(0);
      order.setId(1L);
      return order;
    });

    Session mockSession = mock(Session.class);
    when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/session123");
    when(mockSession.getId()).thenReturn("cs_test_123");
    when(paymentService.createCheckoutSession(anyLong(), anyLong(), anyString()))
        .thenReturn(mockSession);

    doNothing().when(cartService).clearCart(testUser.getId());

    // Act
    Session result = orderService.createOrder(testUser);

    // Assert
    assertNotNull(result);
    // 2 * 12.99 + 1 * 12.99 = 38.97
    verify(paymentService).createCheckoutSession(anyLong(), anyLong(), anyString());
  }

  @Test
  void updateOrderStatus_shouldUpdateStatus() {
    // Arrange
    Long orderId = 1L;
    Order.OrderStatus newStatus = Order.OrderStatus.CONFIRMED;
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
    doNothing().when(emailService).sendOrderConfirmation(anyString(), anyString(), anyString());

    // Act
    Order result = orderService.updateOrderStatus(orderId, newStatus);

    // Assert
    assertNotNull(result);
    assertEquals(newStatus, testOrder.getStatus());
    verify(orderRepository).save(testOrder);
    verify(emailService).sendOrderConfirmation(anyString(), anyString(), anyString());
  }

  @Test
  void updateOrderStatus_shouldSendConfirmationEmail_whenStatusIsConfirmed() {
    // Arrange
    Long orderId = 1L;
    Order.OrderStatus newStatus = Order.OrderStatus.CONFIRMED;
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
    doNothing().when(emailService).sendOrderConfirmation(anyString(), anyString(), anyString());

    // Act
    orderService.updateOrderStatus(orderId, newStatus);

    // Assert
    verify(emailService).sendOrderConfirmation(anyString(), anyString(), anyString());
    verify(emailService, never()).sendOrderStatusUpdate(anyString(), anyString(), anyString());
  }

  @Test
  void updateOrderStatus_shouldSendStatusUpdateEmail_whenStatusIsNotConfirmed() {
    // Arrange
    Long orderId = 1L;
    Order.OrderStatus newStatus = Order.OrderStatus.PREPARING;
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
    doNothing().when(emailService).sendOrderStatusUpdate(anyString(), anyString(), anyString());

    // Act
    orderService.updateOrderStatus(orderId, newStatus);

    // Assert
    verify(emailService).sendOrderStatusUpdate(anyString(), anyString(), anyString());
    verify(emailService, never()).sendOrderConfirmation(anyString(), anyString(), anyString());
  }

  @Test
  void confirmOrder_shouldUpdateOrderToConfirmed() {
    // Arrange
    String stripeSessionId = "cs_test_123";
    testOrder.setStripeSessionId(stripeSessionId);
    when(orderRepository.findByStripeSessionId(stripeSessionId)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
    doNothing().when(emailService).sendOrderConfirmation(anyString(), anyString(), anyString());

    // Act
    Order result = orderService.confirmOrder(stripeSessionId);

    // Assert
    assertNotNull(result);
    assertEquals(Order.OrderStatus.CONFIRMED, testOrder.getStatus());
    verify(orderRepository).save(testOrder);
    verify(emailService).sendOrderConfirmation(anyString(), anyString(), anyString());
  }

  @Test
  void confirmOrder_shouldThrowException_whenOrderNotFound() {
    // Arrange
    String stripeSessionId = "cs_test_invalid";
    when(orderRepository.findByStripeSessionId(stripeSessionId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class, () -> orderService.confirmOrder(stripeSessionId));

    assertTrue(exception.getMessage().contains("Order not found"));
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void findByUserId_shouldReturnUserOrders() {
    // Arrange
    Long userId = 1L;
    Page<Order> expectedPage = new PageImpl<>(Arrays.asList(testOrder));
    when(orderRepository.findByUserId(userId, pageable)).thenReturn(expectedPage);

    // Act
    Page<Order> result = orderService.findByUserId(userId, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(testOrder.getId(), result.getContent().get(0).getId());
    verify(orderRepository).findByUserId(userId, pageable);
  }

  @Test
  void findById_shouldReturnOrder_whenExists() {
    // Arrange
    Long orderId = 1L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

    // Act
    Order result = orderService.findById(orderId);

    // Assert
    assertNotNull(result);
    assertEquals(testOrder.getId(), result.getId());
    verify(orderRepository).findById(orderId);
  }

  @Test
  void findById_shouldThrowException_whenNotExists() {
    // Arrange
    Long orderId = 999L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(orderId));

    assertTrue(exception.getMessage().contains("Order not found"));
    verify(orderRepository).findById(orderId);
  }

  @Test
  void findByStatus_shouldReturnOrdersWithStatus() {
    // Arrange
    Order.OrderStatus status = Order.OrderStatus.CONFIRMED;
    Page<Order> expectedPage = new PageImpl<>(Arrays.asList(testOrder));
    when(orderRepository.findByStatus(status, pageable)).thenReturn(expectedPage);

    // Act
    Page<Order> result = orderService.findByStatus(status, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(orderRepository).findByStatus(status, pageable);
  }
}
