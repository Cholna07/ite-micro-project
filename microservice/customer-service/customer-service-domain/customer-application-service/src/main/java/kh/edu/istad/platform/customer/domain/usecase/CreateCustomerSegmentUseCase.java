package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.CreateCustomerSegmentCommand;
import kh.edu.istad.platform.customer.domain.dto.CustomerSegmentResult;
import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreateCustomerSegmentUseCase {

    private final CustomerSegmentRepository customerSegmentRepository;

    public Mono<CustomerSegmentResult> execute(
            CreateCustomerSegmentCommand command
    ) {
        CustomerSegment segment = new CustomerSegment(
                command.type(),
                command.description()
        );

        segment.initiate();

        return customerSegmentRepository.save(segment)
                .map(this::toResult);
    }

    private CustomerSegmentResult toResult(CustomerSegment segment) {
        return new CustomerSegmentResult(
                segment.getId().value(),
                segment.getType().name(),
                segment.getDescription()
        );
    }
}