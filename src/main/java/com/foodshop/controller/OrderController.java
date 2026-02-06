package com.foodshop.controller;

import com.foodshop.domain.Order;
import com.foodshop.domain.User;
import com.foodshop.security.UserPrincipal;
import com.foodshop.service.OrderService;
import com.stripe.model.checkout.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for order management.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  /**
   * Creates an order and redirects to Stripe checkout.
   *
   * @param principal the authenticated user
   * @param redirectAttributes redirect attributes for flash messages
   * @return redirect to Stripe checkout or cart page
   */
  @PostMapping("/create")
  public String createOrder(
      @AuthenticationPrincipal UserPrincipal principal,
      RedirectAttributes redirectAttributes) {

    try {
      User user = principal.getUser();
      Session session = orderService.createOrder(user);

      // Redirect to Stripe checkout
      return "redirect:" + session.getUrl();
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage",
          "Failed to create order: " + e.getMessage());
      return "redirect:/cart";
    }
  }

  /**
   * Displays order history for the authenticated user.
   *
   * @param principal the authenticated user
   * @param page page number
   * @param model the model
   * @return the order list view
   */
  @GetMapping
  public String orderHistory(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(defaultValue = "0") int page,
      Model model) {

    User user = principal.getUser();
    Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
    Page<Order> orders = orderService.findByUserId(user.getId(), pageable);

    model.addAttribute("orders", orders);
    model.addAttribute("title", "Order History");

    return "orders/list";
  }

  /**
   * Displays order details.
   *
   * @param id the order ID
   * @param principal the authenticated user
   * @param model the model
   * @return the order detail view
   */
  @GetMapping("/{id}")
  public String orderDetail(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal principal,
      Model model) {

    Order order = orderService.findById(id);

    // Verify that the order belongs to the authenticated user
    if (!order.getUser().getId().equals(principal.getUser().getId())) {
      throw new org.springframework.security.access.AccessDeniedException(
          "You are not authorized to view this order");
    }

    model.addAttribute("order", order);
    model.addAttribute("title", "Order Details");

    return "orders/detail";
  }

  /**
   * Handles successful Stripe checkout.
   *
   * @param sessionId the Stripe session ID
   * @param model the model
   * @return the success view
   */
  @GetMapping("/success")
  public String success(@RequestParam("session_id") String sessionId, Model model) {
    try {
      Order order = orderService.confirmOrder(sessionId);
      model.addAttribute("order", order);
      model.addAttribute("title", "Order Confirmed");
      return "orders/success";
    } catch (Exception e) {
      model.addAttribute("errorMessage", "Failed to confirm order");
      return "redirect:/orders";
    }
  }

  /**
   * Handles cancelled Stripe checkout.
   *
   * @param redirectAttributes redirect attributes for flash messages
   * @return redirect to cart page
   */
  @GetMapping("/cancel")
  public String cancel(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("errorMessage",
        "Checkout was cancelled. Your cart has been preserved.");
    return "redirect:/cart";
  }
}
