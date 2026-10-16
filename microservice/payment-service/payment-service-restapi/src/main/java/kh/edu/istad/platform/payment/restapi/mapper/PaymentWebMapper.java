package kh.edu.istad.platform.payment.restapi.mapper;

import kh.edu.istad.platform.payment.domain.dto.CreatePaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.CreatePaymentResponse;
import kh.edu.istad.platform.payment.restapi.dto.CreatePaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class PaymentWebMapper {

    public CreatePaymentCommand toCommand(CreatePaymentRequest request) {
        return new CreatePaymentCommand(
                request.orderId(),
                request.customerId(),
                request.price()
        );
    }

    public kh.edu.istad.platform.payment.restapi.dto.CreatePaymentResponse
    toResponse(CreatePaymentResponse response) {
        return new kh.edu.istad.platform.payment.restapi.dto.CreatePaymentResponse(
                response.paymentId(),
                response.orderId(),
                response.customerId(),
                response.amount(),
                response.currency(),
                response.status(),
                response.createdAt()
        );
    }
}
