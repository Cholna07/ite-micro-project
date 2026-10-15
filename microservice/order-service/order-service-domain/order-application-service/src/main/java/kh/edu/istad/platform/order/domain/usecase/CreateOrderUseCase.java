
package kh.edu.istad.platform.order.domain.usecase;

import kh.edu.istad.platform.order.domain.dto.CreateOrderCommand;
import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;
import kh.edu.istad.platform.order.domain.port.out.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CreateOrderUseCase {

    private final OrderRepository orderRepository;

    public CreateOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public CreateOrderResult createOrder(CreateOrderCommand command) {

        if (command == null) {
            throw new IllegalArgumentException(
                    "Create order command cannot be null"
            );
        }

        if (command.customerId() == null) {
            throw new IllegalArgumentException(
                    "Customer ID cannot be null"
            );
        }

        if (command.businessId() == null) {
            throw new IllegalArgumentException(
                    "Business ID cannot be null"
            );
        }

        if (command.street() == null || command.street().isBlank()
                || command.postalCode() == null || command.postalCode().isBlank()
                || command.city() == null || command.city().isBlank()) {
            throw new IllegalArgumentException(
                    "Delivery address must be complete"
            );
        }

        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException(
                    "Order must contain at least one item"
            );
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<CreateOrderResult.OrderItemResult> items =
                new java.util.ArrayList<>();

        for (CreateOrderCommand.OrderItemCommand item : command.items()) {

            if (item == null || item.productId() == null) {
                throw new IllegalArgumentException(
                        "Product and product ID are required"
                );
            }

            if (item.productName() == null || item.productName().isBlank()) {
                throw new IllegalArgumentException(
                        "Product name is required"
                );
            }

            if (item.quantity() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Quantity must be greater than zero"
                );
            }

            if (item.unitPrice() == null
                    || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Unit price cannot be negative or null"
                );
            }

            BigDecimal subtotal = item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()));

            totalPrice = totalPrice.add(subtotal);

            items.add(new CreateOrderResult.OrderItemResult(
                    item.productId(),
                    item.productName(),
                    item.quantity(),
                    item.unitPrice(),
                    subtotal
            ));
        }

        CreateOrderResult result = new CreateOrderResult(
                UUID.randomUUID(),
                command.customerId(),
                command.businessId(),
                command.street(),
                command.postalCode(),
                command.city(),
                List.copyOf(items),
                totalPrice
        );

        return orderRepository.save(result);
    }
}
