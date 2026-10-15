package kh.edu.istad.platform.order.domain.exception;

public class OrderDomainException extends RuntimeException {
    public OrderDomainException(String message) {
        super(message);
    }
}