package kh.edu.istad.platform.order.domain.valueobject;

import java.math.BigDecimal;

public record Money(BigDecimal amount) {
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Money amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Money amount cannot be negative"
            );
        }
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative"
            );
        }

        return new Money(
                this.amount.multiply(BigDecimal.valueOf(quantity))
        );
    }
}