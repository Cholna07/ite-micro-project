package kh.edu.istad.platform.order.restapi.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        UUID customerId,
        UUID restaurantId,
        String street,
        String postalCode,
        String city,
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice
    ) {
    }
}
