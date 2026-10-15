package kh.edu.istad.platform.order.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {

    @Id
    private UUID orderItemId;
    private UUID productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
}
