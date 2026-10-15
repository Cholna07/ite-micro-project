package kh.edu.istad.platform.order.domain.service;

import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.domain.valueobject.Money;

public interface OrderDomainService {
    void validateAndInitializeOrder(Order order);
    Money calculateTotalPrice(Order order);
}