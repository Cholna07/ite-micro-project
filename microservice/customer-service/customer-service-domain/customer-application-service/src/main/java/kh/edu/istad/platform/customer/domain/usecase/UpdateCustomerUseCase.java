
package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public Mono<UpdateCustomerResult> execute(
            UUID customerId,
            UpdateCustomerCommand command
    ) {
        log.info("Update customer use case: {}", command);

        CustomerId id = new CustomerId(customerId);

        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException(
                                "Customer not found: " + customerId
                        )
                ))
                .flatMap(customer -> {
                    customer.updateCustomer(
                            command.familyName(),
                            command.givenName()
                    );

                    return customerRepository.save(customer);
                })
                .map(updatedCustomer -> new UpdateCustomerResult(
                        updatedCustomer.getId().value(),
                        updatedCustomer.getUsername(),
                        updatedCustomer.getFamilyName(),
                        updatedCustomer.getGivenName(),
                        updatedCustomer.getEmail().value(),
                        updatedCustomer.getPhoneNumber().value(),
                        updatedCustomer.getStatus().name()
                ))
                .doOnSuccess(result ->
                        log.info("Customer updated successfully: {}", customerId)
                )
                .doOnError(error ->
                        log.error(
                                "Failed to update customer: {}",
                                customerId,
                                error
                        )
                );
    }
}
