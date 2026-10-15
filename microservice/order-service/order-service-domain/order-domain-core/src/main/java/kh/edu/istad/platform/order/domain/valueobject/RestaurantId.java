package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;

public record RestaurantId(UUID value) {
    public RestaurantId {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Restaurant ID cannot be null"
            );
        }
    }
}