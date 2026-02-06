package com.foodshop.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.foodshop.domain.*;
import com.foodshop.repository.*;
import com.foodshop.service.CartService;
import com.foodshop.service.FoodItemService;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for cart operations.
 *
 * <p>Tests verify cart functionality with real PostgreSQL and Redis using Testcontainers, ensuring
 * data persistence, cart item management, and session handling work correctly.
 */
@SpringBootTest
@Testcontainers
class CartIntegrationTest {

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

  @Autowired private CartService cartService;

  @Autowired private FoodItemService foodItemService;

  @Autowired private UserRepository userRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private FoodItemRepository foodItemRepository;

  @Autowired private CartRepository cartRepository;

  private User testUser;
  private FoodItem testFoodItem;
  private Category testCategory;

  @BeforeEach
  void setUp() {
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
  }

  @AfterEach
  void tearDown() {
    cartRepository.deleteAll();
    foodItemRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void shouldCreateCartForNewUser() {
    // Act
    Cart cart = cartService.getOrCreateCart(testUser);

    // Assert
    assertNotNull(cart);
    assertNotNull(cart.getId());
    assertEquals(testUser.getId(), cart.getUser().getId());
    assertTrue(cart.getItems().isEmpty());
  }

  @Test
  void shouldReturnExistingCartForUser() {
    // Arrange
    Cart firstCart = cartService.getOrCreateCart(testUser);

    // Act
    Cart secondCart = cartService.getOrCreateCart(testUser);

    // Assert
    assertEquals(firstCart.getId(), secondCart.getId());
  }

  @Test
  void shouldAddItemToCart() {
    // Act
    Cart cart = cartService.addItem(testUser, testFoodItem.getId(), 2);

    // Assert
    assertNotNull(cart);
    assertEquals(1, cart.getItems().size());
    CartItem item = cart.getItems().get(0);
    assertEquals(testFoodItem.getId(), item.getFoodItem().getId());
    assertEquals(2, item.getQuantity());
  }

  @Test
  void shouldUpdateQuantityWhenAddingSameItem() {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 2);

    // Act
    Cart cart = cartService.addItem(testUser, testFoodItem.getId(), 3);

    // Assert
    assertEquals(1, cart.getItems().size());
    CartItem item = cart.getItems().get(0);
    assertEquals(5, item.getQuantity()); // 2 + 3
  }

  @Test
  void shouldUpdateItemQuantity() {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 2);
    Cart cart = cartService.getCartWithItems(testUser.getId());
    Long itemId = cart.getItems().get(0).getId();

    // Act
    Cart updatedCart = cartService.updateItemQuantity(testUser.getId(), itemId, 5);

    // Assert
    assertEquals(1, updatedCart.getItems().size());
    assertEquals(5, updatedCart.getItems().get(0).getQuantity());
  }

  @Test
  void shouldRemoveItemWhenQuantitySetToZero() {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 2);
    Cart cart = cartService.getCartWithItems(testUser.getId());
    Long itemId = cart.getItems().get(0).getId();

    // Act
    Cart updatedCart = cartService.updateItemQuantity(testUser.getId(), itemId, 0);

    // Assert
    assertTrue(updatedCart.getItems().isEmpty());
  }

  @Test
  void shouldRemoveItemFromCart() {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 2);
    Cart cart = cartService.getCartWithItems(testUser.getId());
    Long itemId = cart.getItems().get(0).getId();

    // Act
    Cart updatedCart = cartService.removeItem(testUser.getId(), itemId);

    // Assert
    assertTrue(updatedCart.getItems().isEmpty());
  }

  @Test
  void shouldClearAllItemsFromCart() {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 2);

    // Act
    cartService.clearCart(testUser.getId());

    // Assert
    Cart cart = cartService.getCartWithItems(testUser.getId());
    assertTrue(cart.getItems().isEmpty());
  }

  @Test
  void shouldCalculateCorrectItemCount() {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 3);

    // Act
    int count = cartService.getCartItemCount(testUser.getId());

    // Assert
    assertEquals(3, count);
  }

  @Test
  void shouldPersistCartAcrossSessions() {
    // Arrange
    cartService.addItem(testUser, testFoodItem.getId(), 2);

    // Clear EntityManager cache to simulate new session
    cartRepository.flush();

    // Act
    Cart retrievedCart = cartService.getCartWithItems(testUser.getId());

    // Assert
    assertNotNull(retrievedCart);
    assertEquals(1, retrievedCart.getItems().size());
    assertEquals(2, retrievedCart.getItems().get(0).getQuantity());
  }

  @Test
  void shouldHandleMultipleItemsInCart() {
    // Arrange - Create second food item
    FoodItem secondItem = new FoodItem();
    secondItem.setName("Second Food");
    secondItem.setDescription("Second Description");
    secondItem.setPrice(new BigDecimal("15.99"));
    secondItem.setAvailable(true);
    secondItem.setCategory(testCategory);
    secondItem = foodItemRepository.save(secondItem);

    // Act
    cartService.addItem(testUser, testFoodItem.getId(), 2);
    Cart cart = cartService.addItem(testUser, secondItem.getId(), 1);

    // Assert
    assertEquals(2, cart.getItems().size());
    int totalCount = cartService.getCartItemCount(testUser.getId());
    assertEquals(3, totalCount); // 2 + 1
  }

  @Test
  void shouldThrowExceptionWhenAddingUnavailableItem() {
    // Arrange
    testFoodItem.setAvailable(false);
    foodItemRepository.save(testFoodItem);

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> cartService.addItem(testUser, testFoodItem.getId(), 1));
  }
}
