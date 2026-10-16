package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.platform.order.domain.entity.OrderItem;
import kh.edu.istad.platform.order.domain.exception.OrderDomainException;
import kh.edu.istad.platform.order.domain.valueobject.BusinessId;
import kh.edu.istad.platform.order.domain.valueobject.CustomerId;
import kh.edu.istad.platform.order.domain.valueobject.Money;
import kh.edu.istad.platform.order.domain.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.OrderStatus;
import kh.edu.istad.platform.order.domain.valueobject.StreetAddress;
import kh.edu.istad.platform.order.domain.valueobject.TrackingId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private final BusinessId businessId;
    private final StreetAddress deliveryAddress;
    private final List<OrderItem> items;

    private TrackingId trackingId;
    private OrderStatus status;

    public Order(
            OrderId id,
            CustomerId customerId,
            BusinessId businessId,
            StreetAddress deliveryAddress,
            List<OrderItem> items
    ) {
        if (id == null || customerId == null || businessId == null) {
            throw new OrderDomainException(
                    "Order ID, customer ID, and business ID are required"
            );
        }

        if (deliveryAddress == null) {
            throw new OrderDomainException(
                    "Delivery address is required"
            );
        }

        if (items == null || items.isEmpty()) {
            throw new OrderDomainException(
                    "An order must contain at least one item"
            );
        }

        this.id = id;
        this.customerId = customerId;
        this.businessId = businessId;
        this.deliveryAddress = deliveryAddress;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
    }

    public void validateOrder() {
        if (items.isEmpty()) {
            throw new OrderDomainException(
                    "An order must contain at least one item"
            );
        }

        for (OrderItem item : items) {
            if (item == null) {
                throw new OrderDomainException(
                        "Order items cannot contain null"
                );
            }
        }
    }

    public void initializeOrder() {
        validateOrder();
        this.status = OrderStatus.PENDING;
    }

    public void pay() {
        if (status != OrderStatus.PENDING) {
            throw new OrderDomainException(
                    "Only pending orders can be paid"
            );
        }

        this.status = OrderStatus.PAID;
    }

    public void approve(TrackingId trackingId) {
        if (status != OrderStatus.PAID) {
            throw new OrderDomainException(
                    "Only paid orders can be approved"
            );
        }

        if (trackingId == null) {
            throw new OrderDomainException(
                    "Tracking ID is required to approve an order"
            );
        }

        this.trackingId = trackingId;
        this.status = OrderStatus.APPROVED;
    }

    public void initCancel() {
        if (status != OrderStatus.PENDING
                && status != OrderStatus.PAID) {
            throw new OrderDomainException(
                    "This order cannot begin cancellation"
            );
        }

        this.status = OrderStatus.CANCELLING;
    }

    public void cancel() {
        if (status != OrderStatus.CANCELLING) {
            throw new OrderDomainException(
                    "Order must begin cancellation before it can be cancelled"
            );
        }

        this.status = OrderStatus.CANCELLED;
    }

    public Money getTotalPrice() {
        BigDecimal total = items.stream()
                .map(item -> item.getSubtotal().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Money(total);
    }

    public OrderId getId() {
        return id;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public BusinessId getBusinessId() {
        return businessId;
    }

    public StreetAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
