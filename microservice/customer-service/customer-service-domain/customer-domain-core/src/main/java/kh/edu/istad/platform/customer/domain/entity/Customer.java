package kh.edu.istad.platform.customer.domain.entity;

import kh.edu.istad.common.domain.valueobject.Money;

import java.math.BigDecimal;

public class Customer {
     static void main() {
        Money money = new Money(BigDecimal.valueOf(1000));
        money.isGreaterThanZero();
    }
}
