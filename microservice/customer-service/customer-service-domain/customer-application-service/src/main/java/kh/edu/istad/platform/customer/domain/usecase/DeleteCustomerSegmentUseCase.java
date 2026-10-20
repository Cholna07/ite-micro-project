package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCustomerSegmentUseCase {

    private final CustomerSegmentRepository customerSegmentRepository;

    public Mono<Void> execute(UUID segmentId) {
        CustomerSegmentId id = new CustomerSegmentId(segmentId);

        return customerSegmentRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException(
                                "Customer segment not found: " + segmentId
                        )
                ))
                .then(customerSegmentRepository.deleteById(id));
    }
}