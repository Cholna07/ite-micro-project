package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import kh.edu.istad.platform.customer.domain.valueobject.Email;
import kh.edu.istad.platform.customer.domain.valueobject.PhoneNumber;
import kh.edu.istad.platform.customer.persistence.entity.CustomerEntity;
import kh.edu.istad.platform.customer.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;

    @Override
    public Customer save(Customer customer) {

        CustomerEntity entity = new CustomerEntity();

        entity.setCustomerId(customer.getId().value());
        entity.setUsername(customer.getUsername());
        entity.setFamilyName(customer.getFamilyName());
        entity.setGivenName(customer.getGivenName());
        entity.setEmail(customer.getEmail().value());
        entity.setPhoneNumber(customer.getPhoneNumber().value());
        entity.setStatus(customer.getStatus().name());

        CustomerEntity savedEntity = customerJpaRepository.save(entity);

        return Customer.builder()
                .id(new CustomerId(savedEntity.getCustomerId()))
                .username(savedEntity.getUsername())
                .familyName(savedEntity.getFamilyName())
                .givenName(savedEntity.getGivenName())
                .email(new Email(savedEntity.getEmail()))
                .phoneNumber(new PhoneNumber(savedEntity.getPhoneNumber()))
                .status(CustomerStatus.valueOf(savedEntity.getStatus()))
                .build();
    }

    @Override
    public Customer findById(UUID customerId) {

        CustomerEntity entity = customerJpaRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return Customer.builder()
                .id(new CustomerId(entity.getCustomerId()))
                .username(entity.getUsername())
                .familyName(entity.getFamilyName())
                .givenName(entity.getGivenName())
                .email(new Email(entity.getEmail()))
                .phoneNumber(new PhoneNumber(entity.getPhoneNumber()))
                .status(CustomerStatus.valueOf(entity.getStatus()))
                .build();
    }

    @Override
    public Customer update(Customer customer) {

        CustomerEntity entity = customerJpaRepository.findById(
                customer.getId().value()
        ).orElseThrow(() -> new RuntimeException("Customer not found"));

        entity.setUsername(customer.getUsername());
        entity.setFamilyName(customer.getFamilyName());
        entity.setGivenName(customer.getGivenName());
        entity.setEmail(customer.getEmail().value());
        entity.setPhoneNumber(customer.getPhoneNumber().value());
        entity.setStatus(customer.getStatus().name());

        CustomerEntity updatedEntity = customerJpaRepository.save(entity);

        return Customer.builder()
                .id(new CustomerId(updatedEntity.getCustomerId()))
                .username(updatedEntity.getUsername())
                .familyName(updatedEntity.getFamilyName())
                .givenName(updatedEntity.getGivenName())
                .email(new Email(updatedEntity.getEmail()))
                .phoneNumber(new PhoneNumber(updatedEntity.getPhoneNumber()))
                .status(CustomerStatus.valueOf(updatedEntity.getStatus()))
                .build();
    }
}