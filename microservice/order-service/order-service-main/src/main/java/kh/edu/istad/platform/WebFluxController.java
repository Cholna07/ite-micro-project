package kh.edu.istad.platform;

import kh.edu.istad.platform.config.ServicePropConfig;
import kh.edu.istad.platform.order.domain.dto.CustomerResponse;
import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import kh.edu.istad.platform.order.domain.port.out.client.CustomerServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@RefreshScope
@RestController
@RequiredArgsConstructor
public class WebFluxController {

    private final ServicePropConfig servicePropConfig;
    private final CustomerServiceClient customerServiceClient;


    @GetMapping("/customers")
    public Flux<QueryCustomerResult> queryCustomer() {
        return customerServiceClient.getQueryCustomerResult();
    }
//    @GetMapping("/customer/Id")
//    public Flux<QueryCustomerResultById> queryCustomerById() {
//        return customerServiceClient;
//    }

    @GetMapping("/flux")
    public Flux<String> getFluxData() {
        return Flux.just("A", "B", "C", "D", "E", "F")
                .log()
                .delayElements(Duration.ofSeconds(1));
    }


    @Value("${global.version}")
    private String globalVersion ;

    @GetMapping("/consul/config")
    public Mono<Map<String,Object>>getConsulConfig(){
        return Mono.just(Map.of(
                "globalVersion",globalVersion,
                "message",servicePropConfig.getMessage(),
                "env",servicePropConfig.getEnv(),
                "version",servicePropConfig.getVersion(),
                "baseurl",servicePropConfig.getBaseUrl()));
    }
}
