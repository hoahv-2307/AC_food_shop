package com.foodshop.controller;

import com.foodshop.domain.Cart;
import com.foodshop.domain.User;
import com.foodshop.security.UserPrincipal;
import com.foodshop.service.CartService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for shopping cart operations.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  /**
   * Displays the shopping cart.
   *
   * @param principal the authenticated user
   * @param model the model
   * @return the cart view
   */
  @GetMapping
  public String viewCart(@AuthenticationPrincipal UserPrincipal principal, Model model) {
    User user = principal.getUser();
    Cart cart = cartService.getCartWithItems(user.getId());

    model.addAttribute("cart", cart);
    model.addAttribute("title", "Shopping Cart");

    return "cart/view";
  }

  /**
   * Adds an item to the cart (AJAX endpoint).
   *
   * @param principal the authenticated user
   * @param foodItemId the food item ID
   * @param quantity the quantity
   * @return JSON response
   */
  @PostMapping("/add")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> addItem(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam Long foodItemId,
      @RequestParam(defaultValue = "1") Integer quantity) {

    User user = principal.getUser();
    cartService.addItem(user, foodItemId, quantity);

    int itemCount = cartService.getCartItemCount(user.getId());

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "Item added to cart");
    response.put("cartItemCount", itemCount);

    return ResponseEntity.ok(response);
  }

  /**
   * Updates item quantity in the cart (AJAX endpoint).
   *
   * @param principal the authenticated user
   * @param itemId the cart item ID
   * @param quantity the new quantity
   * @return JSON response
   */
  @PutMapping("/items/{itemId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> updateItem(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable Long itemId,
      @RequestParam Integer quantity) {

    User user = principal.getUser();
    cartService.updateItemQuantity(user.getId(), itemId, quantity);

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "Cart updated");

    return ResponseEntity.ok(response);
  }

  /**
   * Removes an item from the cart (AJAX endpoint).
   *
   * @param principal the authenticated user
   * @param itemId the cart item ID
   * @return JSON response
   */
  @DeleteMapping("/items/{itemId}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> removeItem(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable Long itemId) {

    User user = principal.getUser();
    cartService.removeItem(user.getId(), itemId);

    int itemCount = cartService.getCartItemCount(user.getId());

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "Item removed from cart");
    response.put("cartItemCount", itemCount);

    return ResponseEntity.ok(response);
  }
}
