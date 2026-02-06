package com.foodshop.controller;

import com.foodshop.service.OrderService;
import com.foodshop.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling Stripe webhooks.
 */
@RestController
@RequestMapping("/api/webhook/stripe")
public class StripeWebhookController {

  private static final Logger LOGGER = LoggerFactory.getLogger(StripeWebhookController.class);

  private final PaymentService paymentService;
  private final OrderService orderService;

  public StripeWebhookController(PaymentService paymentService, OrderService orderService) {
    this.paymentService = paymentService;
    this.orderService = orderService;
  }

  /**
   * Handles Stripe webhook events.
   *
   * @param payload the webhook payload
   * @param sigHeader the Stripe signature header
   * @return response entity
   */
  @PostMapping
  public ResponseEntity<String> handleWebhook(
      @RequestBody String payload,
      @RequestHeader("Stripe-Signature") String sigHeader) {

    try {
      Event event = paymentService.constructEvent(payload, sigHeader);

      // Handle the event
      if ("checkout.session.completed".equals(event.getType())) {
        Session session = (Session) event.getDataObjectDeserializer()
            .getObject()
            .orElseThrow(() -> new IllegalStateException("Failed to deserialize session"));

        handleCheckoutSessionCompleted(session);
      }

      return ResponseEntity.ok("Webhook received");
    } catch (Exception e) {
      LOGGER.error("Error processing Stripe webhook", e);
      return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
    }
  }

  private void handleCheckoutSessionCompleted(Session session) {
    try {
      Long orderId = paymentService.extractOrderId(session);
      orderService.confirmOrder(session.getId());
      LOGGER.info("Successfully processed payment for order: {}", orderId);
    } catch (Exception e) {
      LOGGER.error("Failed to process checkout session completion", e);
    }
  }
}
