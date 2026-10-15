package kh.edu.istad.platform.payment.domain.service;

import kh.edu.istad.platform.payment.domain.entity.Payment;

public interface PaymentDomainService {

    void validateAndInitializePayment(Payment payment);
}