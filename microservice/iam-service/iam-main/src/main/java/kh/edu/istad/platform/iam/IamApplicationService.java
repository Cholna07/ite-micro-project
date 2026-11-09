package kh.edu.istad.platform.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "kh.edu.istad.platform")
public class IamApplicationService {
    public static void main(String[] args) {
        SpringApplication.run(IamApplicationService.class, args);
    }
}