package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.platform.order.domain.exception.OrderDomainException;
import kh.edu.istad.platform.order.domain.valueobject.Money;
import kh.edu.istad.platform.order.domain.valueobject.OrderItemId;

import java.util.UUID;

public class OrderItem {
    private final OrderItemId id;
    private final UUID productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(
            OrderItemId id,
            UUID productId,
            String productName,
            int quantity,
            Money unitPrice
    ) {
        if (id == null || productId == null) {
            throw new OrderDomainException(
                    "Order item ID and product ID are required"
            );
        }

        if (productName == null || productName.isBlank()) {
            throw new OrderDomainException("Product name is required");
        }

        if (quantity <= 0) {
            throw new OrderDomainException(
                    "Quantity must be greater than zero"
            );
        }

        if (unitPrice == null) {
            throw new OrderDomainException("Unit price is required");
        }

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }

    public OrderItemId getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }
}