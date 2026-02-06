package com.foodshop.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.foodshop.domain.*;
import com.foodshop.repository.*;
import com.foodshop.service.*;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for order creation flow.
 *
 * <p>Tests verify end-to-end order processing including cart conversion, payment session creation,
 * order status updates, and email notifications with mocked Stripe API.
 */
@SpringBootTest
@Testcontainers
class OrderIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @Container
  static GenericContainer<?> redis =
      new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
  }

  @Autowired private OrderService orderService;

  @Autowired private CartService cartService;

  @MockBean private PaymentService paymentService;

  @MockBean private EmailService emailService;

  @Autowired private UserRepository userRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private FoodItemRepository foodItemRepository;

  @Autowired private CartRepository cartRepository;

  @Autowired private OrderRepository orderRepository;

  private User testUser;
  private FoodItem testFoodItem;
  private Category testCategory;

  @BeforeEach
  void setUp() throws Exception {
    // Create test user
    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");
    testUser.setRole(User.UserRole.CUSTOMER);
    testUser.setProvider(User.OAuthProvider.GOOGLE);
    testUser.setExternalId("google123");
    testUser = userRepository.save(testUser);

    // Create test category
    testCategory = new Category();
    testCategory.setName("Test Category");
    testCategory.setDescription("Test Description");
    testCategory.setActive(true);
    testCategory.setDisplayOrder(1);
    testCategory = categoryRepository.save(testCategory);

    // Create test food item
    testFoodItem = new FoodItem();
    testFoodItem.setName("Test Food");
    testFoodItem.setDescription("Test Description");
    testFoodItem.setPrice(new BigDecimal("10.99"));
    testFoodItem.setAvailable(true);
    testFoodItem.setCategory(testCategory);
    testFoodItem = foodItemRepository.save(testFoodItem);

    // Mock Stripe payment service
    Session mockSession = mock(Session.class);
    when(mockSession.getId()).thenReturn("cs_test_123");
    when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/session123");
    when(paymentService.createCheckoutSession(anyLong(), anyLong(), anyString()))
        .thenReturn(mockSession);

    // Mock email service
    doNothing().when(emailService).sendOrderConfirmation(anyString(), anyString(), anyString());
    doNothing().when(emailService).sendOrderStatusUpdate(anyString(), anyString(), anyString());
  }

  @AfterEach
  void tearDown() {
    orderRepository.deleteAll();
    cartRepository.deleteAll();
    foodItemRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void shouldCreateOrderFromCart() throws Exception {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 2);

    // Act
    Session session = orderService.createOrder(testUser);

    // Assert
    assertNotNull(session);
    assertNotNull(session.getId());
    assertEquals("cs_test_123", session.getId());

    // Verify order was created
    Order order = orderRepository.findByStripeSessionId(session.getId()).orElseThrow();
    assertNotNull(order);
    assertNotNull(order.getId());
    assertEquals(testUser.getId(), order.getUser().getId());
    assertEquals(Order.OrderStatus.PENDING, order.getStatus());
    assertEquals(1, order.getItems().size());
    assertEquals(2, order.getItems().get(0).getQuantity());
    assertEquals("cs_test_123", order.getStripeSessionId());

    // Verify cart was cleared
    Cart cart = cartService.getCartWithItems(testUser.getId());
    assertTrue(cart.getItems().isEmpty());

    // Verify Stripe session was created
    verify(paymentService)
        .createCheckoutSession(eq(order.getId()), anyLong(), eq(testUser.getEmail()));
  }

  @Test
  void shouldCalculateCorrectTotalAmount() throws Exception {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 3);

    // Act
    Session session = orderService.createOrder(testUser);
    Order order = orderRepository.findByStripeSessionId(session.getId()).orElseThrow();

    // Assert
    BigDecimal expectedTotal = testFoodItem.getPrice().multiply(new BigDecimal("3"));
    assertEquals(0, expectedTotal.compareTo(order.getTotalAmount()));
  }

  @Test
  void shouldThrowExceptionWhenCreatingOrderFromEmptyCart() {
    // Act & Assert
    assertThrows(IllegalStateException.class, () -> orderService.createOrder(testUser));
  }

  @Test
  void shouldUpdateOrderStatus() {
    // Arrange
    Order order = new Order();
    order.setUser(testUser);
    order.setStatus(Order.OrderStatus.PENDING);
    order.setTotalAmount(new BigDecimal("10.99"));
    order = orderRepository.save(order);

    // Act
    Order updatedOrder =
        orderService.updateOrderStatus(order.getId(), Order.OrderStatus.CONFIRMED);

    // Assert
    assertEquals(Order.OrderStatus.CONFIRMED, updatedOrder.getStatus());
    verify(emailService).sendOrderConfirmation(anyString(), anyString(), anyString());
  }

  @Test
  void shouldConfirmOrderByStripeSessionId() {
    // Arrange
    Order order = new Order();
    order.setUser(testUser);
    order.setStatus(Order.OrderStatus.PENDING);
    order.setTotalAmount(new BigDecimal("10.99"));
    order.setStripeSessionId("cs_test_456");
    order = orderRepository.save(order);

    // Act
    Order confirmedOrder = orderService.confirmOrder("cs_test_456");

    // Assert
    assertNotNull(confirmedOrder);
    assertEquals(Order.OrderStatus.CONFIRMED, confirmedOrder.getStatus());
    verify(emailService).sendOrderConfirmation(anyString(), anyString(), anyString());
  }

  @Test
  void shouldRetrieveUserOrders() throws Exception {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 1);
    orderService.createOrder(testUser);

    cartService.addItem(testUser, testFoodItem.getId(), 2);
    orderService.createOrder(testUser);

    // Act
    Page<Order> orders = orderService.findByUserId(testUser.getId(), PageRequest.of(0, 10));

    // Assert
    assertNotNull(orders);
    assertEquals(2, orders.getTotalElements());
  }

  @Test
  void shouldFilterOrdersByStatus() throws Exception {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 1);
    Session session1 = orderService.createOrder(testUser);
    Order order1 = orderRepository.findByStripeSessionId(session1.getId()).orElseThrow();

    cartService.addItem(testUser, testFoodItem.getId(), 2);
    Session session2 = orderService.createOrder(testUser);
    Order order2 = orderRepository.findByStripeSessionId(session2.getId()).orElseThrow();

    orderService.updateOrderStatus(order2.getId(), Order.OrderStatus.CONFIRMED);

    // Act
    Page<Order> confirmedOrders =
        orderService.findByStatus(Order.OrderStatus.CONFIRMED, PageRequest.of(0, 10));

    // Assert
    assertEquals(1, confirmedOrders.getTotalElements());
    assertEquals(Order.OrderStatus.CONFIRMED, confirmedOrders.getContent().get(0).getStatus());
  }

  @Test
  void shouldPreserveOrderItemPricesAtTimeOfOrder() throws Exception {
    // Arrange
    BigDecimal originalPrice = testFoodItem.getPrice();
    cartService.addItem(testUser, testFoodItem.getId(), 1);

    // Act
    Session session = orderService.createOrder(testUser);
    Order order = orderRepository.findByStripeSessionId(session.getId()).orElseThrow();

    // Change the food item price
    testFoodItem.setPrice(new BigDecimal("99.99"));
    foodItemRepository.save(testFoodItem);

    // Assert - Order item should have original price
    Order retrievedOrder = orderService.findById(order.getId());
    assertEquals(0, originalPrice.compareTo(retrievedOrder.getItems().get(0).getPrice()));
  }

  @Test
  void shouldHandleMultipleItemsInOrder() throws Exception {
    // Arrange - Create second food item
    FoodItem secondItem = new FoodItem();
    secondItem.setName("Second Food");
    secondItem.setDescription("Second Description");
    secondItem.setPrice(new BigDecimal("15.99"));
    secondItem.setAvailable(true);
    secondItem.setCategory(testCategory);
    secondItem = foodItemRepository.save(secondItem);

    cartService.addItem(testUser, testFoodItem.getId(), 2);
    cartService.addItem(testUser, secondItem.getId(), 1);

    // Act
    Session session = orderService.createOrder(testUser);
    Order order = orderRepository.findByStripeSessionId(session.getId()).orElseThrow();

    // Assert
    assertEquals(2, order.getItems().size());
    BigDecimal expectedTotal =
        testFoodItem
            .getPrice()
            .multiply(new BigDecimal("2"))
            .add(secondItem.getPrice());
    assertEquals(0, expectedTotal.compareTo(order.getTotalAmount()));
  }

  @Test
  void shouldSendDifferentEmailsForDifferentStatuses() {
    // Arrange
    Order order = new Order();
    order.setUser(testUser);
    order.setStatus(Order.OrderStatus.PENDING);
    order.setTotalAmount(new BigDecimal("10.99"));
    order = orderRepository.save(order);

    // Act - Update to CONFIRMED
    orderService.updateOrderStatus(order.getId(), Order.OrderStatus.CONFIRMED);
    verify(emailService, times(1)).sendOrderConfirmation(anyString(), anyString(), anyString());

    // Act - Update to PREPARING
    orderService.updateOrderStatus(order.getId(), Order.OrderStatus.PREPARING);
    verify(emailService, times(1)).sendOrderStatusUpdate(anyString(), anyString(), anyString());
  }
}
