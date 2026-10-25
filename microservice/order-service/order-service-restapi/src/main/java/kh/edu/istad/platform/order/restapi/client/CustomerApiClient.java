
package kh.edu.istad.platform.order.restapi.client;

import kh.edu.istad.platform.order.restapi.dto.CustomerResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CustomerApiClient {

    private final WebClient webClient;
    public CustomerApiClient(
            WebClient.Builder loadBalancedWebClientBuilder
    ) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://CUSTOMER-SERVICE")
                .build();
    }

    public Mono<CustomerResponse> getCustomer(UUID customerId) {
        return webClient.get()
                .uri("/api/customers/{customerId}", customerId)
                .retrieve()
                .bodyToMono(CustomerResponse.class);
    }
}
