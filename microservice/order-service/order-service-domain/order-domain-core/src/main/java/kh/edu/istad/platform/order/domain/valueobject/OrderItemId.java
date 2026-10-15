package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;

public record OrderItemId(UUID value) {
    public OrderItemId {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Order item ID cannot be null"
            );
        }
    }
}