package kh.edu.istad.platform.payment.domain.usecase;

import kh.edu.istad.platform.payment.domain.dto.CreatePaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.CreatePaymentResponse;
import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.port.out.PaymentRepository;
import kh.edu.istad.platform.payment.domain.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.valueobject.Money;
import kh.edu.istad.platform.payment.domain.valueobject.OrderId;
import kh.edu.istad.platform.payment.domain.valueobject.PaymentStatus;
import org.springframework.stereotype.Service;

@Service
public class CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;

    public CreatePaymentUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentCommand command) {

        if (command == null) {
            throw new IllegalArgumentException(
                    "Create payment command cannot be null"
            );
        }

        if (command.orderId() == null) {
            throw new IllegalArgumentException(
                    "Order ID cannot be null"
            );
        }

        if (command.customerId() == null) {
            throw new IllegalArgumentException(
                    "Customer ID cannot be null"
            );
        }

        if (command.price() == null
                || command.price().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment price must be greater than zero"
            );
        }

        Payment payment = new Payment(
                null,
                new OrderId(command.orderId()),
                new CustomerId(command.customerId()),
                new Money(command.price()),
                PaymentStatus.COMPLETED,
                null
        );

        payment.initializePayment();

        Payment savedPayment = paymentRepository.save(payment);

        return new CreatePaymentResponse(
                savedPayment.getPaymentId().value(),
                savedPayment.getOrderId().value(),
                savedPayment.getCustomerId().value(),
                savedPayment.getPrice().amount(),
                "USD",
                savedPayment.getPaymentStatus().name(),
                savedPayment.getCreatedAt()
        );
    }
}
