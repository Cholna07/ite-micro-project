package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerRepository {

    public Mono<Customer> save(Customer customer);
//    Customer findById(UUID customerId);
//    Customer update(Customer customer);
}