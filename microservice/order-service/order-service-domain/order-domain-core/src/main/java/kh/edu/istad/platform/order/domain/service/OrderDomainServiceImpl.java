package kh.edu.istad.platform.order.domain.service;

import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.domain.exception.OrderDomainException;
import kh.edu.istad.platform.order.domain.valueobject.Money;

public class OrderDomainServiceImpl implements OrderDomainService {
    @Override
    public void validateAndInitializeOrder(Order order) {
        if (order == null) {
            throw new OrderDomainException("Order cannot be null");
        }

        order.validateOrder();
        order.initializeOrder();
    }

    @Override
    public Money calculateTotalPrice(Order order) {
        if (order == null) {
            throw new OrderDomainException("Order cannot be null");
        }

        order.validateOrder();
        return order.getTotalPrice();
    }
}