package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;

public record BusinessId(UUID value) {
    public BusinessId {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Business ID cannot be null"
            );
        }
    }
}