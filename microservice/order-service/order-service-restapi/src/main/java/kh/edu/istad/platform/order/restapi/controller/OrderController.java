package kh.edu.istad.platform.order.restapi.controller;

import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;
import kh.edu.istad.platform.order.domain.usecase.CreateOrderUseCase;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderRequest;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderResponse;
import kh.edu.istad.platform.order.restapi.mapper.OrderWebMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderWebMapper orderRestMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse createOrder(
            @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResult result =
                createOrderUseCase.createOrder(
                        orderRestMapper.toCommand(request)
                );

        return orderRestMapper.toResponse(result);
    }
}