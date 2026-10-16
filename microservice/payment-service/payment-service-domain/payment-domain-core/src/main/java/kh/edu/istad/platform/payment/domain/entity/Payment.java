package kh.edu.istad.platform.payment.domain.entity;

import kh.edu.istad.platform.payment.domain.exception.PaymentDomainException;
import kh.edu.istad.platform.payment.domain.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.valueobject.Money;
import kh.edu.istad.platform.payment.domain.valueobject.OrderId;
import kh.edu.istad.platform.payment.domain.valueobject.PaymentId;
import kh.edu.istad.platform.payment.domain.valueobject.PaymentStatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Payment {

    private PaymentId paymentId;
    private OrderId orderId;
    private CustomerId customerId;
    private Money price;
    private PaymentStatus paymentStatus;
    private ZonedDateTime createdAt;

    public Payment() {
    }

    public Payment(
            PaymentId paymentId,
            OrderId orderId,
            CustomerId customerId,
            Money price,
            PaymentStatus paymentStatus,
            ZonedDateTime createdAt
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.price = price;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public void validatePayment() {
        if (orderId == null) {
            throw new PaymentDomainException("Order ID is required");
        }

        if (customerId == null) {
            throw new PaymentDomainException("Customer ID is required");
        }

        if (price == null || price.amount() == null
                || price.amount().signum() <= 0) {
            throw new PaymentDomainException(
                    "Payment amount must be greater than zero"
            );
        }
    }

    public void initializePayment() {
        validatePayment();

        if (paymentId == null) {
            paymentId = new PaymentId(UUID.randomUUID());
        }

        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }

        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.COMPLETED;
        }
    }

    public void updateStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new PaymentDomainException(
                    "Payment status cannot be null"
            );
        }

        this.paymentStatus = paymentStatus;
    }

    public PaymentId getPaymentId() {
        return paymentId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getPrice() {
        return price;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}