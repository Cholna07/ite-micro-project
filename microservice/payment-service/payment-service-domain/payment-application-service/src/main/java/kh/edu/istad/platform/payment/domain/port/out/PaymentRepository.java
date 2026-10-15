package kh.edu.istad.platform.payment.domain.port.out;

import kh.edu.istad.platform.payment.domain.entity.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);
}