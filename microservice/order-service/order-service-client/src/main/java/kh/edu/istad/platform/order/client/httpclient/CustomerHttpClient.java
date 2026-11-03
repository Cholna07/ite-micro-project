package kh.edu.istad.platform.order.client.httpclient;

import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@HttpExchange
public interface CustomerHttpClient {
    @GetExchange("/api/customers")
    Flux<QueryCustomerResult> queryCustomer();
    @GetExchange("/{id}")
    Mono<QueryCustomerResult> getCustomerById (@PathVariable String id);
}
