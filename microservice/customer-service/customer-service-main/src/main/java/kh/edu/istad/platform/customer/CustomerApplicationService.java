
package kh.edu.istad.platform.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class CustomerApplicationService {
    public static void main(String[] args) {
        SpringApplication.run(CustomerApplicationService.class, args);
    }
}
