package kh.edu.istad.platform.order.client.adapter;

import kh.edu.istad.platform.order.client.httpclient.CustomerHttpClient;
import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import kh.edu.istad.platform.order.domain.port.out.client.CustomerServiceClient;
import kh.edu.istad.platform.order.domain.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class CustomerServiceClientAdapter
        implements CustomerServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final CustomerHttpClient customerHttpClient;

    private record ValuePayload(String value) {}


    private record CustomerPayload(
            String username,
            String familyName,
            String givenName,
            String email,
            String phoneNumber,
            String customerStatus
    ) {}


    private WebClient customerServiceClient() {
        return webClientBuilder
                .baseUrl("http://customer-service")
                .build();
    }

    @Override
    public Flux<QueryCustomerResult> getQueryCustomerResult() {
//        return customerServiceClient()
//                .get()
//                .uri("/api/customers")
//                .retrieve()
//                .bodyToFlux(CustomerPayload.class)
//                .map(customerPayload -> new QueryCustomerResult(
//                        customerPayload.familyName
//                ));
        return customerHttpClient.queryCustomer();
    }




    @Override
    public Flux<CustomerResponse> getCustomers() {
        return customerServiceClient()
                .get()
                .uri("/api/customers")
                .retrieve()
                .bodyToFlux(CustomerPayload.class)
                .map(customer -> new CustomerResponse(
                        customer.username(),
                        customer.familyName(),
                        customer.givenName(),
                        customer.email(),
                        customer.phoneNumber(),
                        customer.customerStatus()
                ));
    }


}
