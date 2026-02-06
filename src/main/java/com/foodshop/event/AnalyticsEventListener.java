package com.foodshop.event;

import com.foodshop.domain.Order;
import com.foodshop.domain.OrderItem;
import com.foodshop.service.AnalyticsTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener for analytics-related events.
 *
 * <p>Handles order completion events to track food item order counts asynchronously.
 */
@Component
public class AnalyticsEventListener {

  private static final Logger logger = LoggerFactory.getLogger(AnalyticsEventListener.class);

  private final AnalyticsTrackingService trackingService;

  public AnalyticsEventListener(AnalyticsTrackingService trackingService) {
    this.trackingService = trackingService;
  }

  /**
   * Handles order completed events by incrementing order counts for all items in the order.
   *
   * <p>Executes asynchronously to avoid blocking the order completion flow.
   *
   * @param event the order completed event
   */
  @Async
  @EventListener
  public void handleOrderCompletedEvent(OrderCompletedEvent event) {
    Order order = event.getOrder();
    logger.info("Processing order completed event for order ID: {}", order.getId());

    try {
      for (OrderItem item : order.getItems()) {
        Long foodItemId = item.getFoodItem().getId();
        int quantity = item.getQuantity();
        trackingService.incrementOrderCount(foodItemId, quantity);
        logger.debug(
            "Incremented order count for food item {} by {}", foodItemId, quantity);
      }
      logger.info("Successfully processed analytics for order ID: {}", order.getId());
    } catch (Exception e) {
      logger.error("Failed to update analytics for order ID: {}", order.getId(), e);
      // Don't rethrow - analytics failure shouldn't break order completion
    }
  }
}
