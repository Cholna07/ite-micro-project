package edu.kh.istad.platform.order.persistence.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table("orders")
public class OrderEntity {
    @Id
    private UUID id;
    private UUID business_id;
    private String failure_messages;
    private String order_status;
    private BigDecimal price;
    private UUID tracking_id;
    private UUID order_address_id;
}
