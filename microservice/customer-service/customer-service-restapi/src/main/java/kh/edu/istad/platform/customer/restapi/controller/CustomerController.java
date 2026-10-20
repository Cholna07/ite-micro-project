package kh.edu.istad.platform.customer.restapi.controller;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.*;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.usecase.*;
import kh.edu.istad.platform.customer.restapi.dto.CustomerCreateRequest;
import kh.edu.istad.platform.customer.restapi.dto.CustomerCreateResponse;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final CustomerRepository customerRepository;
    private final CustomerWebMapper customerWebMapper;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeactivateCustomerUseCase deactivateCustomerUseCase;
    private final SearchCustomersUseCase searchCustomersUseCase;
    private final GetCustomerStatisticsUseCase getCustomerStatisticsUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerCreateResponse> createCustomer(
            @RequestBody CustomerCreateRequest request) {
        return createCustomerUseCase
                .create(customerWebMapper.toCommand(request))
                .map(customerWebMapper::toCreateResponse);
    }

    @GetMapping("/statistics")
    public Flux<CustomerStatistics> getStatistics() {
        return getCustomerStatisticsUseCase.execute();
    }

    @GetMapping
    public Flux<Customer> searchCustomers(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "familyName", required = false) String familyName,
            @RequestParam(name = "givenName", required = false) String givenName,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "segmentId", required = false) UUID segmentId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        CustomerFilter filter = new CustomerFilter(
                username,
                familyName,
                givenName,
                email,
                status,
                segmentId
        );

        return searchCustomersUseCase.execute(filter, page, size);
    }

    @GetMapping("/{customerId}")
    public Mono<Customer> getCustomerById(
            @PathVariable("customerId") UUID customerId) {
        return customerRepository.findById(new CustomerId(customerId));
    }

    @PutMapping("/{customerId}")
    public Mono<UpdateCustomerResult> updateCustomer(
            @PathVariable("customerId") UUID customerId,
            @RequestBody UpdateCustomerCommand command) {
        return updateCustomerUseCase.execute(customerId, command);
    }

    @PatchMapping("/{customerId}/deactivate")
    public Mono<DeactivateCustomerResult> deactivateCustomer(
            @PathVariable("customerId") UUID customerId) {
        return deactivateCustomerUseCase.execute(
                new DeactivateCustomerCommand(customerId));
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCustomer(
            @PathVariable("customerId") UUID customerId) {
        CustomerId id = new CustomerId(customerId);

        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Customer not found: " + customerId)))
                .then(customerRepository.deleteById(id));
    }
}
