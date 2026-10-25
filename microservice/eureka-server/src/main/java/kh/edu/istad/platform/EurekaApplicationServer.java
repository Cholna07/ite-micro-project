package kh.edu.istad.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaApplicationServer {
    static void main(String[] args) {
        SpringApplication.run(EurekaApplicationServer.class,args);
    }
}
