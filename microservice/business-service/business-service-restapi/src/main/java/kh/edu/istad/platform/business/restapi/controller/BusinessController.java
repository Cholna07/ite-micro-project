
package kh.edu.istad.platform.business.restapi.controller;

import kh.edu.istad.platform.business.domain.usecase.RegisterBusinessUseCase;
import kh.edu.istad.platform.business.restapi.dto.BusinessResponse;
import kh.edu.istad.platform.business.restapi.dto.CreateBusinessRequest;
import kh.edu.istad.platform.business.restapi.mapper.BusinessRestMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final RegisterBusinessUseCase registerBusinessUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BusinessResponse> createBusiness(
            @Valid @RequestBody CreateBusinessRequest request) {
        return registerBusinessUseCase
                .registerBusiness(BusinessRestMapper.toCommand(request))
                .map(BusinessRestMapper::toResponse);
    }
}
