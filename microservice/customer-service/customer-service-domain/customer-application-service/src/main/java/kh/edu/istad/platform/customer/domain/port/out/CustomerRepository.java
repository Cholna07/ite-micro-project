package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.platform.customer.domain.entity.Customer;

import java.util.UUID;
import java.util.List;

public interface CustomerRepository {
    Customer save(Customer customer);
    Customer findById(UUID customerId);
    Customer update(Customer customer);
    List<Customer> findAll();
}
