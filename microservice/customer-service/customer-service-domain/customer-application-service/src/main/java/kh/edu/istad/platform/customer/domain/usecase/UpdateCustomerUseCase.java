package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public UpdateCustomerResult execute(
            UUID customerId,
            UpdateCustomerCommand command
    ) {

        log.info("update customer usecase: {}", command);

        Customer customer = customerRepository.findById(customerId);

        customer.updateCustomer(
                command.familyName(),
                command.givenName()
        );

        Customer updatedCustomer = customerRepository.update(customer);

        return new UpdateCustomerResult(
                updatedCustomer.getId().value(),
                updatedCustomer.getUsername(),
                updatedCustomer.getFamilyName(),
                updatedCustomer.getGivenName(),
                updatedCustomer.getEmail().value(),
                updatedCustomer.getPhoneNumber().value(),
                updatedCustomer.getStatus().name()
        );
    }
}