package com.foodshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.foodshop.domain.*;
import com.foodshop.exception.ResourceNotFoundException;
import com.foodshop.repository.CartRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for CartService.
 *
 * <p>Tests verify cart operations including creation, item management (add, update, remove),
 * and cart state retrieval.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock private CartRepository cartRepository;

  @Mock private FoodItemService foodItemService;

  @InjectMocks private CartService cartService;

  private User testUser;
  private Cart testCart;
  private FoodItem testFoodItem;
  private CartItem testCartItem;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");

    testCart = new Cart();
    testCart.setId(1L);
    testCart.setUser(testUser);
    testCart.setItems(new ArrayList<>());

    Category category = new Category();
    category.setId(1L);
    category.setName("Pizza");

    testFoodItem = new FoodItem();
    testFoodItem.setId(1L);
    testFoodItem.setName("Margherita Pizza");
    testFoodItem.setPrice(new BigDecimal("12.99"));
    testFoodItem.setAvailable(true);
    testFoodItem.setCategory(category);

    testCartItem = new CartItem();
    testCartItem.setId(1L);
    testCartItem.setCart(testCart);
    testCartItem.setFoodItem(testFoodItem);
    testCartItem.setQuantity(2);
  }

  @Test
  void getOrCreateCart_shouldReturnExistingCart_whenExists() {
    // Arrange
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));

    // Act
    Cart result = cartService.getOrCreateCart(testUser);

    // Assert
    assertNotNull(result);
    assertEquals(testCart.getId(), result.getId());
    assertEquals(testUser.getId(), result.getUser().getId());
    verify(cartRepository).findByUserId(testUser.getId());
    verify(cartRepository, never()).save(any(Cart.class));
  }

  @Test
  void getOrCreateCart_shouldCreateNewCart_whenNotExists() {
    // Arrange
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // Act
    Cart result = cartService.getOrCreateCart(testUser);

    // Assert
    assertNotNull(result);
    verify(cartRepository).findByUserId(testUser.getId());
    verify(cartRepository).save(any(Cart.class));
  }

  @Test
  void getCartWithItems_shouldReturnCartWithItems() {
    // Arrange
    testCart.getItems().add(testCartItem);
    when(cartRepository.findByUserIdWithItems(testUser.getId())).thenReturn(Optional.of(testCart));

    // Act
    Cart result = cartService.getCartWithItems(testUser.getId());

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getItems().size());
    assertEquals(testFoodItem.getName(), result.getItems().get(0).getFoodItem().getName());
    verify(cartRepository).findByUserIdWithItems(testUser.getId());
  }

  @Test
  void addItem_shouldAddNewItem_whenNotInCart() {
    // Arrange
    Long foodItemId = 1L;
    int quantity = 2;
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
    when(foodItemService.findById(foodItemId)).thenReturn(testFoodItem);
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // Act
    Cart result = cartService.addItem(testUser, foodItemId, quantity);

    // Assert
    assertNotNull(result);
    verify(foodItemService).findById(foodItemId);
    verify(cartRepository).save(any(Cart.class));
  }

  @Test
  void addItem_shouldUpdateQuantity_whenItemExistsInCart() {
    // Arrange
    Long foodItemId = 1L;
    int additionalQuantity = 3;
    testCart.getItems().add(testCartItem);
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
    when(foodItemService.findById(foodItemId)).thenReturn(testFoodItem);
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // Act
    Cart result = cartService.addItem(testUser, foodItemId, additionalQuantity);

    // Assert
    assertNotNull(result);
    assertEquals(5, testCartItem.getQuantity()); // 2 + 3
    verify(cartRepository).save(any(Cart.class));
  }

  @Test
  void addItem_shouldThrowException_whenFoodItemNotAvailable() {
    // Arrange
    Long foodItemId = 1L;
    int quantity = 2;
    testFoodItem.setAvailable(false);
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
    when(foodItemService.findById(foodItemId)).thenReturn(testFoodItem);

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> cartService.addItem(testUser, foodItemId, quantity));

    assertTrue(exception.getMessage().contains("not available"));
    verify(cartRepository, never()).save(any(Cart.class));
  }

  @Test
  void updateItemQuantity_shouldUpdateQuantity() {
    // Arrange
    Long itemId = 1L;
    int newQuantity = 5;
    testCart.getItems().add(testCartItem);
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // Act
    Cart result = cartService.updateItemQuantity(testUser.getId(), itemId, newQuantity);

    // Assert
    assertNotNull(result);
    assertEquals(newQuantity, testCartItem.getQuantity());
    verify(cartRepository).save(testCart);
  }

  @Test
  void updateItemQuantity_shouldRemoveItem_whenQuantityIsZeroOrNegative() {
    // Arrange
    Long itemId = 1L;
    int newQuantity = 0;
    testCart.getItems().add(testCartItem);
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // Act
    Cart result = cartService.updateItemQuantity(testUser.getId(), itemId, newQuantity);

    // Assert
    assertNotNull(result);
    assertTrue(testCart.getItems().isEmpty());
    verify(cartRepository).save(testCart);
  }

  @Test
  void removeItem_shouldRemoveItemFromCart() {
    // Arrange
    Long itemId = 1L;
    testCart.getItems().add(testCartItem);
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // Act
    Cart result = cartService.removeItem(testUser.getId(), itemId);

    // Assert
    assertNotNull(result);
    assertTrue(testCart.getItems().isEmpty());
    verify(cartRepository).save(testCart);
  }

  @Test
  void clearCart_shouldRemoveAllItems() {
    // Arrange
    testCart.getItems().add(testCartItem);
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // Act
    cartService.clearCart(testUser.getId());

    // Assert
    assertTrue(testCart.getItems().isEmpty());
    verify(cartRepository).save(testCart);
  }

  @Test
  void getCartItemCount_shouldReturnTotalQuantity() {
    // Arrange
    CartItem item2 = new CartItem();
    item2.setQuantity(3);
    testCart.getItems().add(testCartItem); // quantity: 2
    testCart.getItems().add(item2); // quantity: 3
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));

    // Act
    int result = cartService.getCartItemCount(testUser.getId());

    // Assert
    assertEquals(5, result); // 2 + 3
    verify(cartRepository).findByUserId(testUser.getId());
  }

  @Test
  void getCartItemCount_shouldReturnZero_whenCartIsEmpty() {
    // Arrange
    when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));

    // Act
    int result = cartService.getCartItemCount(testUser.getId());

    // Assert
    assertEquals(0, result);
    verify(cartRepository).findByUserId(testUser.getId());
  }

  @Test
  void getCartWithItems_shouldThrowException_whenCartNotFound() {
    // Arrange
    Long userId = 999L;
    when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> cartService.getCartWithItems(userId));

    assertTrue(exception.getMessage().contains("Cart not found"));
    verify(cartRepository).findByUserIdWithItems(userId);
  }
}
