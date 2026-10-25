
package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.common.domain.entity.AggregateRoot;
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

public class Order extends AggregateRoot<OrderId> {

    private final CustomerId customerId;
    private final BusinessId businessId;
    private final StreetAddress deliveryAddress;
    private final List<OrderItem> items;

    private TrackingId trackingId;
    private OrderStatus status;

    private Order(Builder builder) {
        super.setId(builder.id);

        if (builder.id == null
                || builder.customerId == null
                || builder.businessId == null) {
            throw new OrderDomainException(
                    "Order ID, customer ID, and business ID are required"
            );
        }

        if (builder.deliveryAddress == null) {
            throw new OrderDomainException(
                    "Delivery address is required"
            );
        }

        if (builder.items == null || builder.items.isEmpty()) {
            throw new OrderDomainException(
                    "Order must contain at least one item"
            );
        }

        this.customerId = builder.customerId;
        this.businessId = builder.businessId;
        this.deliveryAddress = builder.deliveryAddress;
        this.items = new ArrayList<>(builder.items);
        this.status = OrderStatus.PENDING;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void validateOrder() {
        if (items.isEmpty()) {
            throw new OrderDomainException(
                    "Order must contain at least one item"
            );
        }

        if (getTotalPrice().amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderDomainException(
                    "Order total must be greater than zero"
            );
        }
    }

    public void initializeOrder() {
        validateOrder();
        status = OrderStatus.PENDING;
    }

    public void pay() {
        if (status != OrderStatus.PENDING) {
            throw new OrderDomainException(
                    "Only pending orders can be paid"
            );
        }

        status = OrderStatus.PAID;
    }

    public void approve(TrackingId trackingId) {
        if (status != OrderStatus.PAID) {
            throw new OrderDomainException(
                    "Only paid orders can be approved"
            );
        }

        if (trackingId == null) {
            throw new OrderDomainException(
                    "Tracking ID is required"
            );
        }

        this.trackingId = trackingId;
        status = OrderStatus.APPROVED;
    }

    public void initCancel() {
        if (status != OrderStatus.PENDING
                && status != OrderStatus.PAID) {
            throw new OrderDomainException(
                    "Order cannot be cancelled in its current status"
            );
        }

        status = OrderStatus.CANCELLING;
    }

    public void cancel() {
        if (status != OrderStatus.CANCELLING) {
            throw new OrderDomainException(
                    "Order must be marked for cancellation first"
            );
        }

        status = OrderStatus.CANCELLED;
    }

    public Money getTotalPrice() {
        BigDecimal total = items.stream()
                .map(item -> item.getSubtotal().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Money(total);
    }

    public OrderId getId() {
        return super.getId();
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

    public List<OrderItem> getOrderItems() {
        return getItems();
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public static final class Builder {

        private OrderId id;
        private CustomerId customerId;
        private BusinessId businessId;
        private StreetAddress deliveryAddress;
        private List<OrderItem> items = new ArrayList<>();

        private Builder() {
        }

        public Builder id(OrderId id) {
            this.id = id;
            return this;
        }

        public Builder customerId(CustomerId customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder businessId(BusinessId businessId) {
            this.businessId = businessId;
            return this;
        }

        public Builder deliveryAddress(StreetAddress deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Builder orderItems(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
