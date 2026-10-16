package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;

public record OrderId(UUID value) {
    public OrderId {
        if (value == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
    }
}