
package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository {

    // Create or persist a customer
    Mono<Customer> save(Customer customer);

    // Find one customer by ID
    Mono<Customer> findById(CustomerId customerId);

    // Retrieve all customers
    Flux<Customer> findAll();

    // Check whether a customer exists
    Mono<Boolean> existsById(CustomerId customerId);

    // Delete a customer by ID
    Mono<Void> deleteById(CustomerId customerId);
}
