package kh.edu.istad.platform.order.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID orderId;

    private UUID customerId;

    private UUID restaurantId;

    private String street;

    private String postalCode;

    private String city;

    private BigDecimal totalPrice;

    @Setter(AccessLevel.NONE)
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItemEntity> items = new ArrayList<>();

    // Constructor used by OrderRepositoryAdapter
    public OrderEntity(
            UUID orderId,
            UUID customerId,
            UUID restaurantId,
            String street,
            String postalCode,
            String city,
            BigDecimal totalPrice
    ) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.street = street;
        this.postalCode = postalCode;
        this.city = city;
        this.totalPrice = totalPrice;
        this.items = new ArrayList<>();
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }
}
