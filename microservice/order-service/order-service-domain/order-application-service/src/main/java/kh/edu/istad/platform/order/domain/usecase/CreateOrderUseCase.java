
package kh.edu.istad.platform.order.domain.usecase;

import kh.edu.istad.platform.order.domain.dto.CreateOrderCommand;
import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;
import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.domain.entity.OrderItem;
import kh.edu.istad.platform.order.domain.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.valueobject.BusinessId;
import kh.edu.istad.platform.order.domain.valueobject.CustomerId;
import kh.edu.istad.platform.order.domain.valueobject.Money;
import kh.edu.istad.platform.order.domain.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.OrderItemId;
import kh.edu.istad.platform.order.domain.valueobject.StreetAddress;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
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

        // Convert command items into domain OrderItem objects
        List<OrderItem> orderItems = command.items().stream()
                .map(item -> new OrderItem(
                        new OrderItemId(UUID.randomUUID()),
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        new Money(item.unitPrice())
                ))
                .toList();

        // Create the Order domain entity using the builder
        Order order = Order.builder()
                .id(new OrderId(UUID.randomUUID()))
                .customerId(new CustomerId(command.customerId()))
                .businessId(new BusinessId(command.businessId()))
                .deliveryAddress(
                        new StreetAddress(
                                command.street(),
                                command.postalCode(),
                                command.city()
                        )
                )
                .items(orderItems)
                .build();

        // Save the order
        Order savedOrder = orderRepository.save(order);

        // Convert the saved entity into the result DTO
        return new CreateOrderResult(
                savedOrder.getId().value(),
                savedOrder.getCustomerId().value(),
                savedOrder.getBusinessId().value(),
                savedOrder.getDeliveryAddress().street(),
                savedOrder.getDeliveryAddress().postalCode(),
                savedOrder.getDeliveryAddress().city(),
                savedOrder.getItems().stream()
                        .map(item -> new CreateOrderResult.OrderItemResult(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice().amount(),
                                item.getSubtotal().amount()
                        ))
                        .toList(),
                savedOrder.getTotalPrice().amount()
        );
    }
}
