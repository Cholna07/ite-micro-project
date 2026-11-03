package kh.edu.istad.platform.customer.bean;

import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainServiceImpl;
import kh.edu.istad.platform.customer.domain.usecase.GetCustomersUseCase;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public GetCustomersUseCase getCustomersUseCase(CustomerRepository customerRepository) {
        return new GetCustomersUseCase(customerRepository);
    }

    @Bean
    public CustomerDomainService customerDomainService() {
        return new CustomerDomainServiceImpl();
    }
}
