package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetCustomersUseCase {

    private final CustomerRepository customerRepository;

    public List<Customer> execute() {
        return customerRepository.findAll();
    }
}
