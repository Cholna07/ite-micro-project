package kh.edu.istad.platform.customer.restapi.controller;

import kh.edu.istad.platform.customer.domain.dto.DeactivateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.DeactivateCustomerResult;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.usecase.DeactivateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.GetCustomersUseCase;
import kh.edu.istad.platform.customer.domain.usecase.UpdateCustomerUseCase;
import kh.edu.istad.platform.customer.restapi.dto.*;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {

    private final InitiateCustomerUseCase initiateCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeactivateCustomerUseCase deactivateCustomerUseCase;
    private final CustomerWebMapper customerWebMapper;
    private final GetCustomersUseCase getCustomersUseCase;

//    @GetMapping
//    public Flux<Customer> finAll(){
//        return
//    }

    @GetMapping
    public List<CustomerResponse> getCustomers() {
        return getCustomersUseCase.execute().stream()
                .map(customer -> new CustomerResponse(
                        customer.getUsername(),
                        customer.getFamilyName(),
                        customer.getGivenName(),
                        customer.getEmail() != null
                                ? customer.getEmail().value()
                                : null,
                        customer.getPhoneNumber() != null
                                ? customer.getPhoneNumber().value()
                                : null,
                        customer.getStatus().name()
                ))
                .toList();
    }



    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CustomerInitiateResponse initiateCustomer(
            @RequestBody CustomerInitiateRequest customerInitiateRequest
    ) {

        InitiateCustomerResult result = initiateCustomerUseCase.execute(
                customerWebMapper.toCommand(customerInitiateRequest)
        );

        return customerWebMapper.toResponse(result);
    }

    @PutMapping("/{customerId}")
    public CustomerUpdateResponse updateCustomer(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody CustomerUpdateRequest customerUpdateRequest
    ) {

        UpdateCustomerResult result = updateCustomerUseCase.execute(
                customerId,
                customerWebMapper.toCommand(customerUpdateRequest)
        );

        return customerWebMapper.toResponse(result);
    }

    @PatchMapping("/{customerId}/deactivate")
    public CustomerDeactivateResponse deactivateCustomer(
            @PathVariable("customerId") UUID customerId
    ) {

        DeactivateCustomerResult result =
                deactivateCustomerUseCase.execute(
                        new DeactivateCustomerCommand(customerId)
                );

        return customerWebMapper.toResponse(result);
    }
}
