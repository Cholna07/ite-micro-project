package kh.edu.istad.platform.order.domain.port.out;

import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    CreateOrderResult save(CreateOrderResult order);

    Optional<CreateOrderResult> findById(UUID orderId);
}
