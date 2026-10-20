package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.CustomerStatistics;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Component
@RequiredArgsConstructor
public class GetCustomerStatisticsUseCase {

    private final CustomerRepository customerRepository;

    public Flux<CustomerStatistics> execute() {
        return customerRepository.getStatistics();
    }
}