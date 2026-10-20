
package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.DeactivateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.DeactivateCustomerResult;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DeactivateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public Mono<DeactivateCustomerResult> execute(
            DeactivateCustomerCommand command
    ) {
        CustomerId customerId = new CustomerId(command.customerId());

        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException(
                                "Customer not found: " + command.customerId()
                        )
                ))
                .flatMap(customer -> {
                    customer.deactivateCustomer();
                    return customerRepository.save(customer);
                })
                .map(updatedCustomer -> new DeactivateCustomerResult(
                        updatedCustomer.getId().value(),
                        updatedCustomer.getUsername(),
                        updatedCustomer.getFamilyName(),
                        updatedCustomer.getGivenName(),
                        updatedCustomer.getEmail().value(),
                        updatedCustomer.getPhoneNumber().value(),
                        updatedCustomer.getStatus().name()
                ));
    }
}
