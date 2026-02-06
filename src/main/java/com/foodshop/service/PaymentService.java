package com.foodshop.service;

import com.foodshop.exception.PaymentException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for handling Stripe payment operations.
 */
@Service
public class PaymentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

  @Value("${app.stripe.success-url}")
  private String successUrl;

  @Value("${app.stripe.cancel-url}")
  private String cancelUrl;

  @Value("${app.stripe.webhook-secret}")
  private String webhookSecret;

  /**
   * Creates a Stripe checkout session.
   *
   * @param orderId the order ID
   * @param amount the amount in dollars
   * @param customerEmail the customer email
   * @return the checkout session
   * @throws PaymentException if session creation fails
   */
  public Session createCheckoutSession(Long orderId, Long amount, String customerEmail) {
    try {
      SessionCreateParams params = SessionCreateParams.builder()
          .setMode(SessionCreateParams.Mode.PAYMENT)
          .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
          .setCancelUrl(cancelUrl)
          .setCustomerEmail(customerEmail)
          .addLineItem(
              SessionCreateParams.LineItem.builder()
                  .setPriceData(
                      SessionCreateParams.LineItem.PriceData.builder()
                          .setCurrency("usd")
                          .setUnitAmount(amount)
                          .setProductData(
                              SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                  .setName("Food Shop Order #" + orderId)
                                  .build()
                          )
                          .build()
                  )
                  .setQuantity(1L)
                  .build()
          )
          .putMetadata("orderId", orderId.toString())
          .build();

      Session session = Session.create(params);
      LOGGER.info("Created Stripe checkout session: {} for order: {}", session.getId(), orderId);
      return session;
    } catch (StripeException e) {
      LOGGER.error("Failed to create Stripe checkout session for order: {}", orderId, e);
      throw new PaymentException("Failed to create checkout session", e);
    }
  }

  /**
   * Verifies and parses a Stripe webhook event.
   *
   * @param payload the webhook payload
   * @param sigHeader the Stripe signature header
   * @return the verified event
   * @throws PaymentException if signature verification fails
   */
  public Event constructEvent(String payload, String sigHeader) {
    try {
      return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    } catch (Exception e) {
      LOGGER.error("Failed to verify Stripe webhook signature", e);
      throw new PaymentException("Invalid webhook signature", e);
    }
  }

  /**
   * Extracts order ID from Stripe session metadata.
   *
   * @param session the Stripe session
   * @return the order ID
   * @throws PaymentException if order ID not found
   */
  public Long extractOrderId(Session session) {
    Map<String, String> metadata = session.getMetadata();
    if (metadata == null || !metadata.containsKey("orderId")) {
      throw new PaymentException("Order ID not found in session metadata");
    }

    try {
      return Long.parseLong(metadata.get("orderId"));
    } catch (NumberFormatException e) {
      throw new PaymentException("Invalid order ID in session metadata", e);
    }
  }
}
