
package kh.edu.istad.platform.order.restapi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CustomerApiClient {

    private static final String CUSTOMER_SERVICE_URL = "http://localhost:9090";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public CustomerApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl(CUSTOMER_SERVICE_URL)
                .build();

        this.objectMapper = new ObjectMapper()
                .findAndRegisterModules();
    }



    public Mono<CustomerResponse> getCustomer(UUID customerId) {
        String url = CUSTOMER_SERVICE_URL + "/api/customers/" + customerId;
        System.out.println("Requesting URL: " + url);

        return webClient.get()
                .uri("/api/customers/{customerId}", customerId)
                .exchangeToMono(response -> {
                    System.out.println("Customer API status: "
                            + response.statusCode());

                    return response.bodyToMono(String.class)
                            .doOnNext(body ->
                                    System.out.println(
                                            "Customer API raw response: " + body))
                            .flatMap(body -> {
                                try {
                                    CustomerResponse customer =
                                            objectMapper.readValue(
                                                    body,
                                                    CustomerResponse.class
                                            );
                                    System.out.println(
                                            "Customer received: " + customer);
                                    return Mono.just(customer);
                                } catch (Exception error) {
                                    return Mono.error(error);
                                }
                            });
                })
                .doOnError(error ->
                        System.err.println(
                                "Customer API error: " + error.getMessage()))
                .switchIfEmpty(Mono.defer(() -> {
                    System.err.println(
                            "Customer API returned an empty response");
                    return Mono.empty();
                }));
    }



    public record CustomerResponse(
            Email email,
            String familyName,
            String givenName,
            CustomerId id,
            PhoneNumber phoneNumber,
            SegmentId segmentId,
            String status,
            String username
    ) {}

    public record Email(String value) {}

    public record CustomerId(UUID value) {}

    public record PhoneNumber(String value) {}

    public record SegmentId(UUID value) {}
}
