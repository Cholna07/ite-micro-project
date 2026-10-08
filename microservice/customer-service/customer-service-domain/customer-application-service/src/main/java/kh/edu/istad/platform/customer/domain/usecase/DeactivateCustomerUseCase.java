package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.DeactivateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.DeactivateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public DeactivateCustomerResult execute(
            DeactivateCustomerCommand command
    ) {

        Customer customer =
                customerRepository.findById(command.customerId());

        customer.deactivateCustomer();

        Customer updatedCustomer =
                customerRepository.update(customer);

        return new DeactivateCustomerResult(
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