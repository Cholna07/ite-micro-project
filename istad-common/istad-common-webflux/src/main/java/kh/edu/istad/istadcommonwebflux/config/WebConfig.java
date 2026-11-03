package kh.edu.istad.istadcommonwebflux.config;

import kh.edu.istad.istadcommonwebflux.exception.DomainExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class WebConfig {
    @Bean
    public DomainExceptionHandler bean(){
        return new DomainExceptionHandler();
    }
}
