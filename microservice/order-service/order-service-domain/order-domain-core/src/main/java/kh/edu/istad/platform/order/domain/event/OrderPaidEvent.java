package kh.edu.istad.platform.order.domain.event;

import kh.edu.istad.platform.order.domain.valueobject.OrderId;

import java.time.Instant;

public record OrderPaidEvent(
        OrderId orderId,
        Instant paidAt
) {
    public OrderPaidEvent {
        if (orderId == null || paidAt == null) {
            throw new IllegalArgumentException(
                    "Order ID and payment time are required"
            );
        }
    }
}