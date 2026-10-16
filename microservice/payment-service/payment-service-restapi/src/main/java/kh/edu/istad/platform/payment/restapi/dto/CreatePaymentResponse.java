package kh.edu.istad.platform.payment.restapi.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record CreatePaymentResponse(
        UUID paymentId,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        String currency,
        String status,
        ZonedDateTime createdAt
) {
}
