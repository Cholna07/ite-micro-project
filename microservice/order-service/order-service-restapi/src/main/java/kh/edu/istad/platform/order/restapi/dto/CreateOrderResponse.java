
package kh.edu.istad.platform.order.restapi.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId,
        UUID customerId,
        UUID restaurantId,
        String street,
        String postalCode,
        String city,
        List<OrderItemResponse> items,
        BigDecimal totalPrice
) {
    public record OrderItemResponse(
            UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
