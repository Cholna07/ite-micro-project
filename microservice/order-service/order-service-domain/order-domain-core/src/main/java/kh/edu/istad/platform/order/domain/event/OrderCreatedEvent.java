package kh.edu.istad.platform.order.domain.event;

import kh.edu.istad.platform.order.domain.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.CustomerId;
import kh.edu.istad.platform.order.domain.valueobject.BusinessId;

import java.time.Instant;

public record OrderCreatedEvent(
        OrderId orderId,
        CustomerId customerId,
        BusinessId businessId,
        Instant createdAt
) {
    public OrderCreatedEvent {
        if (orderId == null || customerId == null || businessId == null) {
            throw new IllegalArgumentException(
                    "Order, customer, and business IDs are required"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Event creation time is required"
            );
        }
    }
}