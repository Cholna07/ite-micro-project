package kh.edu.istad.platform.order.domain.port.out;

import kh.edu.istad.platform.order.domain.entity.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);
    Optional<Order> findById(UUID orderId);
}
