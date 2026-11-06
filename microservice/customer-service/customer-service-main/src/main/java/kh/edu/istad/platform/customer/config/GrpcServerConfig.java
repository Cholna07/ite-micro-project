package kh.edu.istad.platform.customer.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import kh.edu.istad.platform.customer.grpc.CustomerGrpcService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcServerConfig {

    @Bean
    public Server grpcServer(CustomerGrpcService customerGrpcService)
            throws IOException {

        Server server = ServerBuilder
                .forPort(9090)
                .addService(customerGrpcService)
                .build();

        server.start();

        return server;
    }
}