package kh.edu.istad.platform.order.domain.event;

import kh.edu.istad.platform.order.domain.valueobject.OrderId;

import java.time.Instant;

public record OrderCancelledEvent(
        OrderId orderId,
        Instant cancelledAt,
        String reason
) {
    public OrderCancelledEvent {
        if (orderId == null || cancelledAt == null) {
            throw new IllegalArgumentException(
                    "Order ID and cancellation time are required"
            );
        }
    }
}