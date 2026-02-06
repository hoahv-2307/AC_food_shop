package com.foodshop.util;

import com.foodshop.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Utility class for building test data entities.
 *
 * <p>Provides convenient methods for creating test entities with default values.
 */
public class TestDataBuilder {

  /**
   * Creates a test user with default values.
   *
   * @return test user
   */
  public static User createTestUser() {
    User user = new User();
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setProvider(User.OAuthProvider.GOOGLE);
    user.setExternalId("google-123");
    user.setRole(User.UserRole.CUSTOMER);
    user.setStatus(User.UserStatus.ACTIVE);
    user.setCreatedAt(LocalDateTime.now());
    return user;
  }

  /**
   * Creates a test admin user.
   *
   * @return test admin user
   */
  public static User createTestAdmin() {
    User admin = createTestUser();
    admin.setEmail("admin@example.com");
    admin.setName("Test Admin");
    admin.setRole(User.UserRole.ADMIN);
    return admin;
  }

  /**
   * Creates a test category with default values.
   *
   * @return test category
   */
  public static Category createTestCategory() {
    Category category = new Category();
    category.setName("Test Category");
    category.setDescription("Test category description");
    category.setDisplayOrder(1);
    category.setActive(true);
    category.setCreatedAt(LocalDateTime.now());
    return category;
  }

  /**
   * Creates a test food item with the given category.
   *
   * @param category the category for the food item
   * @return test food item
   */
  public static FoodItem createTestFoodItem(Category category) {
    FoodItem foodItem = new FoodItem();
    foodItem.setName("Test Food Item");
    foodItem.setDescription("Test food item description");
    foodItem.setPrice(new BigDecimal("10.99"));
    foodItem.setCategory(category);
    foodItem.setAvailable(true);
    foodItem.setAvgRating(BigDecimal.ZERO);
    foodItem.setRatingCount(0);
    foodItem.setCreatedAt(LocalDateTime.now());
    return foodItem;
  }

  /**
   * Creates a test cart for the given user.
   *
   * @param user the user who owns the cart
   * @return test cart
   */
  public static Cart createTestCart(User user) {
    Cart cart = new Cart();
    cart.setUser(user);
    cart.setCreatedAt(LocalDateTime.now());
    return cart;
  }

  /**
   * Creates a test cart item.
   *
   * @param cart the cart
   * @param foodItem the food item
   * @param quantity the quantity
   * @return test cart item
   */
  public static CartItem createTestCartItem(Cart cart, FoodItem foodItem, int quantity) {
    CartItem cartItem = new CartItem();
    cartItem.setCart(cart);
    cartItem.setFoodItem(foodItem);
    cartItem.setQuantity(quantity);
    cartItem.setAddedAt(LocalDateTime.now());
    return cartItem;
  }

  /**
   * Creates a test order for the given user.
   *
   * @param user the user who placed the order
   * @return test order
   */
  public static Order createTestOrder(User user) {
    Order order = new Order();
    order.setUser(user);
    order.setStatus(Order.OrderStatus.PENDING);
    order.setTotalAmount(BigDecimal.ZERO);
    order.setCreatedAt(LocalDateTime.now());
    return order;
  }

  /**
   * Creates a test order item.
   *
   * @param order the order
   * @param foodItem the food item
   * @param quantity the quantity
   * @param price the price at time of order
   * @return test order item
   */
  public static OrderItem createTestOrderItem(Order order, FoodItem foodItem, int quantity,
      BigDecimal price) {
    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setFoodItem(foodItem);
    orderItem.setQuantity(quantity);
    orderItem.setPrice(price);
    return orderItem;
  }

  /**
   * Creates a test rating.
   *
   * @param user the user who created the rating
   * @param foodItem the food item being rated
   * @param stars the rating (1-5)
   * @return test rating
   */
  public static Rating createTestRating(User user, FoodItem foodItem, int stars) {
    Rating rating = new Rating();
    rating.setUser(user);
    rating.setFoodItem(foodItem);
    rating.setStars(stars);
    rating.setReviewText("Test review");
    rating.setVerifiedPurchase(false);
    rating.setCreatedAt(LocalDateTime.now());
    return rating;
  }
}
