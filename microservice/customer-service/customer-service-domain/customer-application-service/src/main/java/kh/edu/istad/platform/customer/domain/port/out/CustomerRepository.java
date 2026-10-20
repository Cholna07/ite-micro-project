package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.CustomerFilter;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatistics;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository {

    Mono<Customer> save(Customer customer);
    Mono<Customer> findById(CustomerId customerId);
    Flux<Customer> findAll();
    Flux<Customer> findAll(CustomerFilter filter, int page, int size);
    Mono<Boolean> existsById(CustomerId customerId);
    Mono<Void> deleteById(CustomerId customerId);
    Flux<CustomerStatistics> getStatistics();
}