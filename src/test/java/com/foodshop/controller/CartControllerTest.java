package com.foodshop.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.foodshop.domain.*;
import com.foodshop.security.UserPrincipal;
import com.foodshop.service.CartService;
import com.foodshop.service.FoodItemService;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contract tests for CartController API endpoints.
 *
 * <p>Tests verify HTTP request/response contracts for cart operations including adding items,
 * updating quantities, and removing items. Uses MockMvc for isolated controller testing.
 */
@WebMvcTest(CartController.class)
class CartControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CartService cartService;

  @MockBean private FoodItemService foodItemService;

  private User testUser;
  private Cart testCart;
  private FoodItem testFoodItem;

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

    CartItem cartItem = new CartItem();
    cartItem.setId(1L);
    cartItem.setCart(testCart);
    cartItem.setFoodItem(testFoodItem);
    cartItem.setQuantity(2);
    testCart.getItems().add(cartItem);
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void viewCart_shouldReturnCartView() throws Exception {
    // Arrange
    when(cartService.getCartWithItems(anyLong())).thenReturn(testCart);

    // Act & Assert
    mockMvc
        .perform(get("/cart"))
        .andExpect(status().isOk())
        .andExpect(view().name("cart/view"))
        .andExpect(model().attributeExists("cart"));

    verify(cartService).getCartWithItems(anyLong());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void addItem_shouldReturnSuccessJson() throws Exception {
    // Arrange
    when(cartService.addItem(any(User.class), eq(1L), eq(2))).thenReturn(testCart);
    when(cartService.getCartItemCount(anyLong())).thenReturn(4);

    // Act & Assert
    mockMvc
        .perform(post("/cart/add").param("foodItemId", "1").param("quantity", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.cartItemCount").value(4));

    verify(cartService).addItem(any(User.class), eq(1L), eq(2));
    verify(cartService).getCartItemCount(anyLong());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void addItem_shouldReturnErrorJson_whenServiceFails() throws Exception {
    // Arrange
    when(cartService.addItem(any(User.class), eq(1L), eq(2)))
        .thenThrow(new IllegalStateException("Item not available"));

    // Act & Assert
    mockMvc
        .perform(post("/cart/add").param("foodItemId", "1").param("quantity", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Item not available"));

    verify(cartService).addItem(any(User.class), eq(1L), eq(2));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void updateItem_shouldReturnSuccessJson() throws Exception {
    // Arrange
    when(cartService.updateItemQuantity(anyLong(), eq(1L), eq(3))).thenReturn(testCart);
    when(cartService.getCartItemCount(anyLong())).thenReturn(3);

    // Act & Assert
    mockMvc
        .perform(put("/cart/items/1").param("quantity", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.cartItemCount").value(3));

    verify(cartService).updateItemQuantity(anyLong(), eq(1L), eq(3));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void updateItem_shouldReturnErrorJson_whenServiceFails() throws Exception {
    // Arrange
    when(cartService.updateItemQuantity(anyLong(), eq(1L), eq(3)))
        .thenThrow(new IllegalStateException("Invalid quantity"));

    // Act & Assert
    mockMvc
        .perform(put("/cart/items/1").param("quantity", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid quantity"));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void removeItem_shouldReturnSuccessJson() throws Exception {
    // Arrange
    testCart.getItems().clear();
    when(cartService.removeItem(anyLong(), eq(1L))).thenReturn(testCart);
    when(cartService.getCartItemCount(anyLong())).thenReturn(0);

    // Act & Assert
    mockMvc
        .perform(delete("/cart/items/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.cartItemCount").value(0));

    verify(cartService).removeItem(anyLong(), eq(1L));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void removeItem_shouldReturnErrorJson_whenServiceFails() throws Exception {
    // Arrange
    when(cartService.removeItem(anyLong(), eq(1L)))
        .thenThrow(new IllegalStateException("Item not found"));

    // Act & Assert
    mockMvc
        .perform(delete("/cart/items/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Item not found"));
  }

  @Test
  void viewCart_shouldRedirectToLogin_whenNotAuthenticated() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/cart")).andExpect(status().is3xxRedirection());
  }

  @Test
  void addItem_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
    // Act & Assert
    mockMvc
        .perform(post("/cart/add").param("foodItemId", "1").param("quantity", "2"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void addItem_shouldValidateRequiredParameters() throws Exception {
    // Act & Assert - Missing foodItemId
    mockMvc
        .perform(post("/cart/add").param("quantity", "2"))
        .andExpect(status().isBadRequest());

    // Act & Assert - Missing quantity
    mockMvc.perform(post("/cart/add").param("foodItemId", "1")).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void updateItem_shouldValidateQuantityParameter() throws Exception {
    // Act & Assert - Missing quantity
    mockMvc.perform(put("/cart/items/1")).andExpect(status().isBadRequest());
  }
}
