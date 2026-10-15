package kh.edu.istad.platform.payment.domain.service;

import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.exception.PaymentDomainException;

public class PaymentDomainServiceImpl implements PaymentDomainService {

    @Override
    public void validateAndInitializePayment(Payment payment) {
        if (payment == null) {
            throw new PaymentDomainException("Payment cannot be null");
        }

        payment.validatePayment();
        payment.initializePayment();
    }
}