package edu.kh.istad.platform.order.persistence.repository;

import edu.kh.istad.platform.order.persistence.entity.OrderEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface OrderReactiveRepository extends R2dbcRepository<OrderEntity, UUID> {
}
