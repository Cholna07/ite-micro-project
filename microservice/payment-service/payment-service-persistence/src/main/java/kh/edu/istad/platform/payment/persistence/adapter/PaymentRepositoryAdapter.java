package kh.edu.istad.platform.payment.persistence.adapter;

import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.port.out.PaymentRepository;
import kh.edu.istad.platform.payment.domain.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.valueobject.Money;
import kh.edu.istad.platform.payment.domain.valueobject.OrderId;
import kh.edu.istad.platform.payment.domain.valueobject.PaymentId;
import kh.edu.istad.platform.payment.domain.valueobject.PaymentStatus;
import kh.edu.istad.platform.payment.persistence.entity.PaymentPersistenceEntity;
import kh.edu.istad.platform.payment.persistence.repository.PaymentJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryAdapter(
            PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentPersistenceEntity entity = new PaymentPersistenceEntity();

        entity.setPaymentId(payment.getPaymentId().value());
        entity.setOrderId(payment.getOrderId().value());
        entity.setCustomerId(payment.getCustomerId().value());
        entity.setPrice(payment.getPrice().amount());
        entity.setPaymentStatus(payment.getPaymentStatus().name());
        entity.setCreatedAt(payment.getCreatedAt());

        PaymentPersistenceEntity saved =
                paymentJpaRepository.save(entity);

        return new Payment(
                new PaymentId(saved.getPaymentId()),
                new OrderId(saved.getOrderId()),
                new CustomerId(saved.getCustomerId()),
                new Money(saved.getPrice()),
                PaymentStatus.valueOf(saved.getPaymentStatus()),
                saved.getCreatedAt()
        );
    }
}