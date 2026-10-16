package kh.edu.istad.platform.order.persistence.adapter;

import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.domain.entity.OrderItem;
import kh.edu.istad.platform.order.domain.exception.OrderDomainException;
import kh.edu.istad.platform.order.domain.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.valueobject.*;
import kh.edu.istad.platform.order.persistence.entity.OrderEntity;
import kh.edu.istad.platform.order.persistence.entity.OrderItemEntity;
import kh.edu.istad.platform.order.persistence.repository.OrderJpaRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {

        OrderEntity entity = new OrderEntity(
                order.getId().value(),
                order.getCustomerId().value(),
                order.getBusinessId().value(),
                order.getDeliveryAddress().street(),
                order.getDeliveryAddress().postalCode(),
                order.getDeliveryAddress().city(),
                order.getTotalPrice().amount()
        );

        for (OrderItem item : order.getItems()) {
            entity.addItem(new OrderItemEntity(
                    item.getId().value(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice().amount(),
                    null
            ));
        }

        OrderEntity saved = orderJpaRepository.save(entity);

        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId)
                .map(entity -> {
                    List<OrderItem> items = entity.getItems().stream()
                            .map(item -> new OrderItem(
                                    new OrderItemId(item.getOrderItemId()),
                                    item.getProductId(),
                                    item.getProductName(),
                                    item.getQuantity(),
                                    new Money(item.getUnitPrice())
                            ))
                            .toList();

                    return new Order(
                            new OrderId(entity.getOrderId()),
                            new CustomerId(entity.getCustomerId()),
                            new BusinessId(entity.getBusinessId()),
                            new StreetAddress(
                                    entity.getStreet(),
                                    entity.getPostalCode(),
                                    entity.getCity()
                            ),
                            items
                    );
                });
    }
}
