package kh.edu.istad.platform.order.domain.port.out.client;

import kh.edu.istad.platform.order.domain.dto.CustomerResponse;
import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerServiceClient {
    Flux<QueryCustomerResult> getQueryCustomerResult();
    Flux<CustomerResponse> getCustomers();
//    Mono<QueryCustomerResult> getCustomerById();

}