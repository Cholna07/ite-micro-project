package edu.kh.istad.platform.order.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table("order_items")
public class OrderItemEntity {
    @Id
    @Embedded.Nullable
    private OrderIteamEntityId orderIteamEntityId;
    private BigDecimal price;
    private UUID product_id;
    private Integer quantity;
    private BigDecimal sub_total;
    private UUID order_id;
}
