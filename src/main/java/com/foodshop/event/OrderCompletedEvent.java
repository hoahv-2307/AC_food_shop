package com.foodshop.event;

import com.foodshop.domain.Order;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an order is completed.
 * Used to trigger analytics tracking for order counts.
 */
public class OrderCompletedEvent extends ApplicationEvent {
    
    private final Order order;
    
    /**
     * Create a new OrderCompletedEvent.
     *
     * @param source the component that published the event
     * @param order the completed order
     */
    public OrderCompletedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    
    /**
     * Get the completed order.
     *
     * @return the order that was completed
     */
    public Order getOrder() {
        return order;
    }
}
