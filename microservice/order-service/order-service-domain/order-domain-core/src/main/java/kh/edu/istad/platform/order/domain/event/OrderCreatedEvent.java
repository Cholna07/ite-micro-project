package kh.edu.istad.platform.order.domain.event;

import kh.edu.istad.platform.order.domain.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.CustomerId;
import kh.edu.istad.platform.order.domain.valueobject.RestaurantId;

import java.time.Instant;

public record OrderCreatedEvent(
        OrderId orderId,
        CustomerId customerId,
        RestaurantId restaurantId,
        Instant createdAt
) {
    public OrderCreatedEvent {
        if (orderId == null || customerId == null || restaurantId == null) {
            throw new IllegalArgumentException(
                    "Order, customer, and restaurant IDs are required"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Event creation time is required"
            );
        }
    }
}