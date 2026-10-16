package kh.edu.istad.platform.order.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderResult(
        UUID orderId,
        UUID customerId,
        UUID businessId,
        String street,
        String postalCode,
        String city,
        List<OrderItemResult> items,
        BigDecimal totalPrice
) {
    public record OrderItemResult(
            UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}
}
