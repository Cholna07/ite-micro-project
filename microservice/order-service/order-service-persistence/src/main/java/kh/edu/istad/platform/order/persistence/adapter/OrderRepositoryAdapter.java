package kh.edu.istad.platform.order.persistence.adapter;

import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;
import kh.edu.istad.platform.order.domain.port.out.OrderRepository;
import kh.edu.istad.platform.order.persistence.entity.OrderEntity;
import kh.edu.istad.platform.order.persistence.entity.OrderItemEntity;
import kh.edu.istad.platform.order.persistence.repository.OrderJpaRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    public CreateOrderResult save(CreateOrderResult order) {
        OrderEntity entity = new OrderEntity(
                order.orderId(),
                order.customerId(),
                order.restaurantId(),
                order.street(),
                order.postalCode(),
                order.city(),
                order.totalPrice()
        );

        for (CreateOrderResult.OrderItemResult item : order.items()) {
            entity.addItem(new OrderItemEntity(
                    UUID.randomUUID(),
                    item.productId(),
                    item.productName(),
                    item.quantity(),
                    item.unitPrice(),
                    null
            ));
        }

        OrderEntity saved = orderJpaRepository.save(entity);

        return new CreateOrderResult(
                saved.getOrderId(),
                saved.getCustomerId(),
                saved.getRestaurantId(),
                saved.getStreet(),
                saved.getPostalCode(),
                saved.getCity(),
                order.items(),
                saved.getTotalPrice()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreateOrderResult> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId)
                .map(entity -> new CreateOrderResult(
                        entity.getOrderId(),
                        entity.getCustomerId(),
                        entity.getRestaurantId(),
                        entity.getStreet(),
                        entity.getPostalCode(),
                        entity.getCity(),
                        entity.getItems().stream()
                                .map(item ->
                                        new CreateOrderResult.OrderItemResult(
                                                item.getProductId(),
                                                item.getProductName(),
                                                item.getQuantity(),
                                                item.getUnitPrice(),
                                                item.getUnitPrice().multiply(
                                                        java.math.BigDecimal.valueOf(
                                                                item.getQuantity()
                                                        )
                                                )
                                        )
                                )
                                .toList(),
                        entity.getTotalPrice()
                ));
    }
}