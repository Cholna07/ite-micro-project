package kh.edu.istad.platform.customer.restapi.controller;

import kh.edu.istad.platform.customer.domain.usecase.CreateCustomerSegmentUseCase;
import kh.edu.istad.platform.customer.domain.usecase.DeleteCustomerSegmentUseCase;
import kh.edu.istad.platform.customer.domain.usecase.GetAllCustomerSegmentsUseCase;
import kh.edu.istad.platform.customer.domain.usecase.UpdateCustomerSegmentUseCase;
import kh.edu.istad.platform.customer.restapi.dto.CustomerSegmentCreateRequest;
import kh.edu.istad.platform.customer.restapi.dto.CustomerSegmentResponse;
import kh.edu.istad.platform.customer.restapi.dto.CustomerSegmentUpdateRequest;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerSegmentWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer-segments")
public class CustomerSegmentController {

    private final CreateCustomerSegmentUseCase createUseCase;
    private final GetAllCustomerSegmentsUseCase getAllUseCase;
    private final UpdateCustomerSegmentUseCase updateUseCase;
    private final DeleteCustomerSegmentUseCase deleteUseCase;
    private final CustomerSegmentWebMapper customerSegmentWebMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerSegmentResponse> create(
            @RequestBody CustomerSegmentCreateRequest request
    ) {
        return createUseCase
                .execute(customerSegmentWebMapper.toCommand(request))
                .map(customerSegmentWebMapper::toResponse);
    }

    @GetMapping
    public Flux<CustomerSegmentResponse> getAll() {
        return getAllUseCase.execute()
                .map(customerSegmentWebMapper::toResponse);
    }

    @PutMapping("/{segmentId}")
    public Mono<CustomerSegmentResponse> update(
            @PathVariable("segmentId") UUID segmentId,
            @RequestBody CustomerSegmentUpdateRequest request
    ) {
        return updateUseCase
                .execute(segmentId, customerSegmentWebMapper.toCommand(request))
                .map(customerSegmentWebMapper::toResponse);
    }

    @DeleteMapping("/{segmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
            @PathVariable("segmentId") UUID segmentId
    ) {
        return deleteUseCase.execute(segmentId);
    }
}
