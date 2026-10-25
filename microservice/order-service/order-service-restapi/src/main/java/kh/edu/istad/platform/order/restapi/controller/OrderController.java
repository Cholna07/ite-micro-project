
package kh.edu.istad.platform.order.restapi.controller;

import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;
import kh.edu.istad.platform.order.domain.usecase.CreateOrderUseCase;
import kh.edu.istad.platform.order.restapi.client.CustomerApiClient;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderRequest;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderResponse;
import kh.edu.istad.platform.order.restapi.mapper.OrderWebMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderWebMapper orderRestMapper;
    private final CustomerApiClient customerApiClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request
    ) {
        return customerApiClient.getCustomer(request.customerId())
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("Customer not found")
                ))
                .flatMap(customer -> {
                    CreateOrderResult result =
                            createOrderUseCase.createOrder(
                                    orderRestMapper.toCommand(request)
                            );

                    return Mono.just(orderRestMapper.toResponse(result));
                });
    }
}
