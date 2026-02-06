package com.foodshop.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.foodshop.domain.*;
import com.foodshop.service.OrderService;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contract tests for OrderController API endpoints.
 *
 * <p>Tests verify HTTP request/response contracts for order operations including creation,
 * retrieval, and status handling. Uses MockMvc for isolated controller testing.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private OrderService orderService;

  private User testUser;
  private Order testOrder;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");

    testOrder = new Order();
    testOrder.setId(1L);
    testOrder.setUser(testUser);
    testOrder.setStatus(Order.OrderStatus.PENDING);
    testOrder.setTotalAmount(new BigDecimal("25.99"));
    testOrder.setStripeSessionId("cs_test_123");
    testOrder.setItems(new ArrayList<>());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void createOrder_shouldRedirectToStripeCheckout() throws Exception {
    // Arrange
    Session mockSession = mock(Session.class);
    when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/session123");
    when(orderService.createOrder(any(User.class))).thenReturn(mockSession);

    // Act & Assert
    mockMvc
        .perform(post("/orders/create"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("https://checkout.stripe.com/session123"));

    verify(orderService).createOrder(any(User.class));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void createOrder_shouldRedirectToCart_whenOrderCreationFails() throws Exception {
    // Arrange
    when(orderService.createOrder(any(User.class)))
        .thenThrow(new IllegalStateException("Cart is empty"));

    // Act & Assert
    mockMvc
        .perform(post("/orders/create"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"))
        .andExpect(flash().attributeExists("error"));

    verify(orderService).createOrder(any(User.class));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void orderHistory_shouldReturnOrderListView() throws Exception {
    // Arrange
    Page<Order> ordersPage = new PageImpl<>(java.util.List.of(testOrder));
    when(orderService.findByUserId(anyLong(), any(PageRequest.class))).thenReturn(ordersPage);

    // Act & Assert
    mockMvc
        .perform(get("/orders"))
        .andExpect(status().isOk())
        .andExpect(view().name("orders/list"))
        .andExpect(model().attributeExists("orders"));

    verify(orderService).findByUserId(anyLong(), any(PageRequest.class));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void orderHistory_shouldHandlePagination() throws Exception {
    // Arrange
    Page<Order> ordersPage = new PageImpl<>(java.util.List.of(testOrder));
    when(orderService.findByUserId(anyLong(), any(PageRequest.class))).thenReturn(ordersPage);

    // Act & Assert
    mockMvc
        .perform(get("/orders").param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(view().name("orders/list"));

    verify(orderService).findByUserId(eq(1L), eq(PageRequest.of(1, 10)));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void orderDetail_shouldReturnOrderDetailView() throws Exception {
    // Arrange
    when(orderService.findById(1L)).thenReturn(testOrder);

    // Act & Assert
    mockMvc
        .perform(get("/orders/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("orders/detail"))
        .andExpect(model().attributeExists("order"));

    verify(orderService).findById(1L);
  }

  @Test
  @WithMockUser(username = "test2@example.com", authorities = "USER")
  void orderDetail_shouldReturn403_whenUserDoesNotOwnOrder() throws Exception {
    // Arrange
    when(orderService.findById(1L)).thenReturn(testOrder);

    // Act & Assert - Different user trying to access order
    mockMvc.perform(get("/orders/1")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void success_shouldConfirmOrderAndShowSuccessPage() throws Exception {
    // Arrange
    testOrder.setStatus(Order.OrderStatus.CONFIRMED);
    when(orderService.confirmOrder("cs_test_123")).thenReturn(testOrder);

    // Act & Assert
    mockMvc
        .perform(get("/orders/success").param("session_id", "cs_test_123"))
        .andExpect(status().isOk())
        .andExpect(view().name("orders/success"))
        .andExpect(model().attributeExists("order"));

    verify(orderService).confirmOrder("cs_test_123");
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void success_shouldHandleMissingSessionId() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/orders/success"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/orders"));
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void cancel_shouldRedirectToCartWithErrorMessage() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/orders/cancel"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"))
        .andExpect(flash().attributeExists("error"));
  }

  @Test
  void createOrder_shouldRedirectToLogin_whenNotAuthenticated() throws Exception {
    // Act & Assert
    mockMvc.perform(post("/orders/create")).andExpect(status().is3xxRedirection());
  }

  @Test
  void orderHistory_shouldRedirectToLogin_whenNotAuthenticated() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/orders")).andExpect(status().is3xxRedirection());
  }

  @Test
  void orderDetail_shouldRedirectToLogin_whenNotAuthenticated() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/orders/1")).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "test@example.com")
  void createOrder_shouldUsePostMethod() throws Exception {
    // Act & Assert - GET should not be allowed
    mockMvc.perform(get("/orders/create")).andExpect(status().isMethodNotAllowed());
  }
}
