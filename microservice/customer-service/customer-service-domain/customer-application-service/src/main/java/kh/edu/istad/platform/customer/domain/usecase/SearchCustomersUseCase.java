
package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.CustomerFilter;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class SearchCustomersUseCase {

    private final CustomerRepository customerRepository;

    public Flux<Customer> execute(
            CustomerFilter filter,
            int page,
            int size
    ) {
        return customerRepository.findAll(filter, page, size);
    }
}
