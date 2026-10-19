package kh.edu.istad.platform.customer.persistence.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customer")

public class CustomerEntity {
    @Id
    private UUID customerId;
    private String username;
    private String familyName;
    private String givenName;
    private String email;
    private String phoneNumber;
    private String status;
}