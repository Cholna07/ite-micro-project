package kh.edu.istad.platform.payment.domain.usecase;

import kh.edu.istad.platform.payment.domain.dto.CreatePaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.CreatePaymentResponse;
import kh.edu.istad.platform.payment.domain.port.out.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;

    public CreatePaymentUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public CreatePaymentResponse createPayment(CreatePaymentCommand command) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "Create payment command cannot be null"
            );
        }

        // Payment processing will be added in the next step.
        throw new UnsupportedOperationException(
                "Payment processing is not implemented yet"
        );
    }
}
