package kh.edu.istad.platform.order.restapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class FluxController {
    @GetMapping("/flux")
    public Flux<String> getFluxdata(){
        return Flux.just("A","B","C")
                .log()
                .delayElements(Duration.ofSeconds(1));
    }
}
