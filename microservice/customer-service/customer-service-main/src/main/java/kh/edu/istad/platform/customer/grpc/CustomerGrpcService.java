package kh.edu.istad.platform.customer.grpc;


import io.grpc.stub.StreamObserver;
import kh.edu.istad.common.grpc.customer.v1.Customer;
import kh.edu.istad.common.grpc.customer.v1.CustomerServiceGrpc;
import kh.edu.istad.common.grpc.customer.v1.GetCustomersRequest;
import kh.edu.istad.common.grpc.customer.v1.GetCustomersResponse;
import kh.edu.istad.platform.customer.domain.usecase.GetCustomersUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerGrpcService
        extends CustomerServiceGrpc.CustomerServiceImplBase {

    private final GetCustomersUseCase getCustomersUseCase;

    @Override
    public void getCustomers(
            GetCustomersRequest request,
            StreamObserver<GetCustomersResponse> responseObserver
    ) {

        var customers = getCustomersUseCase.execute();

        var response = GetCustomersResponse.newBuilder();

        for (var customer : customers) {

            Customer grpcCustomer = Customer.newBuilder()
                    .setCustomerId(customer.getId().toString())
                    .setUsername(customer.getUsername())
                    .setFamilyName(customer.getFamilyName())
                    .setGivenName(customer.getGivenName())
                    .setEmail(customer.getEmail() != null
                            ? customer.getEmail().value() : "")
                    .setPhoneNumber(customer.getPhoneNumber() != null
                            ? customer.getPhoneNumber().value() : "")
                    .setStatus(customer.getStatus().name())
                    .build();

            response.addCustomers(grpcCustomer);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
