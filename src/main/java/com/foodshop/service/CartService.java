package com.foodshop.service;

import com.foodshop.domain.Cart;
import com.foodshop.domain.CartItem;
import com.foodshop.domain.FoodItem;
import com.foodshop.domain.User;
import com.foodshop.exception.ResourceNotFoundException;
import com.foodshop.repository.CartRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing shopping carts.
 */
@Service
@Transactional
public class CartService {

  private final CartRepository cartRepository;
  private final FoodItemService foodItemService;

  public CartService(CartRepository cartRepository, FoodItemService foodItemService) {
    this.cartRepository = cartRepository;
    this.foodItemService = foodItemService;
  }

  /**
   * Gets or creates a cart for the given user.
   *
   * @param user the user
   * @return the user's cart
   */
  public Cart getOrCreateCart(User user) {
    return cartRepository.findByUserId(user.getId())
        .orElseGet(() -> {
          Cart cart = new Cart();
          cart.setUser(user);
          return cartRepository.save(cart);
        });
  }

  /**
   * Gets a cart with all items loaded for the given user.
   *
   * @param userId the user ID
   * @return the cart with items
   * @throws ResourceNotFoundException if cart not found
   */
  @Transactional(readOnly = true)
  public Cart getCartWithItems(Long userId) {
    return cartRepository.findByUserIdWithItems(userId)
        .orElseThrow(() -> ResourceNotFoundException.forField("Cart", "userId", userId));
  }

  /**
   * Adds an item to the cart or updates quantity if it already exists.
   *
   * @param user the user
   * @param foodItemId the food item ID
   * @param quantity the quantity to add
   * @return the updated cart
   */
  public Cart addItem(User user, Long foodItemId, Integer quantity) {
    Cart cart = getOrCreateCart(user);
    FoodItem foodItem = foodItemService.findById(foodItemId);

    if (!foodItem.getAvailable()) {
      throw new IllegalStateException("Food item is not available");
    }

    // Check if item already in cart
    Optional<CartItem> existingItem = cart.getItems().stream()
        .filter(item -> item.getFoodItem().getId().equals(foodItemId))
        .findFirst();

    if (existingItem.isPresent()) {
      // Update quantity
      CartItem item = existingItem.get();
      item.setQuantity(item.getQuantity() + quantity);
    } else {
      // Add new item
      CartItem newItem = new CartItem();
      newItem.setFoodItem(foodItem);
      newItem.setQuantity(quantity);
      cart.addItem(newItem);
    }

    return cartRepository.save(cart);
  }

  /**
   * Updates the quantity of an item in the cart.
   *
   * @param userId the user ID
   * @param cartItemId the cart item ID
   * @param quantity the new quantity
   * @return the updated cart
   */
  public Cart updateItemQuantity(Long userId, Long cartItemId, Integer quantity) {
    Cart cart = getCartWithItems(userId);

    CartItem item = cart.getItems().stream()
        .filter(ci -> ci.getId().equals(cartItemId))
        .findFirst()
        .orElseThrow(() -> ResourceNotFoundException.forEntity("CartItem", cartItemId));

    if (quantity <= 0) {
      cart.removeItem(item);
    } else {
      item.setQuantity(quantity);
    }

    return cartRepository.save(cart);
  }

  /**
   * Removes an item from the cart.
   *
   * @param userId the user ID
   * @param cartItemId the cart item ID
   * @return the updated cart
   */
  public Cart removeItem(Long userId, Long cartItemId) {
    Cart cart = getCartWithItems(userId);

    CartItem item = cart.getItems().stream()
        .filter(ci -> ci.getId().equals(cartItemId))
        .findFirst()
        .orElseThrow(() -> ResourceNotFoundException.forEntity("CartItem", cartItemId));

    cart.removeItem(item);
    return cartRepository.save(cart);
  }

  /**
   * Clears all items from the cart.
   *
   * @param userId the user ID
   */
  public void clearCart(Long userId) {
    Cart cart = getCartWithItems(userId);
    cart.clear();
    cartRepository.save(cart);
  }

  /**
   * Gets the number of items in the cart.
   *
   * @param userId the user ID
   * @return the item count
   */
  @Transactional(readOnly = true)
  public int getCartItemCount(Long userId) {
    return cartRepository.findByUserIdWithItems(userId)
        .map(cart -> cart.getItems().size())
        .orElse(0);
  }
}
