package kh.edu.istad.platform.payment.restapi.controller;

import kh.edu.istad.platform.payment.domain.usecase.CreatePaymentUseCase;
import kh.edu.istad.platform.payment.restapi.dto.CreatePaymentRequest;
import kh.edu.istad.platform.payment.restapi.dto.CreatePaymentResponse;
import kh.edu.istad.platform.payment.restapi.mapper.PaymentWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final PaymentWebMapper paymentWebMapper;

    public PaymentController(
            CreatePaymentUseCase createPaymentUseCase,
            PaymentWebMapper paymentWebMapper) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.paymentWebMapper = paymentWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePaymentResponse createPayment(
            @RequestBody CreatePaymentRequest request) {

        var command = paymentWebMapper.toCommand(request);
        var response = createPaymentUseCase.createPayment(command);

        return paymentWebMapper.toResponse(response);
    }
}
