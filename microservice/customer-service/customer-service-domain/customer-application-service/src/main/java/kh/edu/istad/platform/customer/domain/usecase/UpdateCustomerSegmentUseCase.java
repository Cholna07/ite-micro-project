package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.CustomerSegmentResult;
import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerSegmentCommand;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentId;
import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCustomerSegmentUseCase {

    private final CustomerSegmentRepository customerSegmentRepository;

    public Mono<CustomerSegmentResult> execute(
            UUID segmentId,
            UpdateCustomerSegmentCommand command
    ) {
        CustomerSegmentId id = new CustomerSegmentId(segmentId);

        return customerSegmentRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException(
                                "Customer segment not found: " + segmentId
                        )
                ))
                .flatMap(segment -> {
                    segment.update(command.type(), command.description());
                    return customerSegmentRepository.save(segment);
                })
                .map(segment -> new CustomerSegmentResult(
                        segment.getId().value(),
                        segment.getType().name(),
                        segment.getDescription()
                ));
    }
}