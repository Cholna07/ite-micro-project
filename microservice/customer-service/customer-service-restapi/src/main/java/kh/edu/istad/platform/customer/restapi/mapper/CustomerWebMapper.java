
package kh.edu.istad.platform.customer.restapi.mapper;

import kh.edu.istad.platform.customer.domain.dto.*;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentId;
import kh.edu.istad.platform.customer.domain.valueobject.Email;
import kh.edu.istad.platform.customer.domain.valueobject.PhoneNumber;
import kh.edu.istad.platform.customer.restapi.dto.*;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CustomerWebMapper {

    InitiateCustomerCommand toCommand(CustomerInitiateRequest request);

    CustomerInitiateResponse toResponse(InitiateCustomerResult result);

    Customer toCommand(CustomerCreateRequest request);

    CustomerCreateResponse toCreateResponse(Customer customer);

    UpdateCustomerCommand toCommand(CustomerUpdateRequest request);

    CustomerUpdateResponse toResponse(UpdateCustomerResult result);

    DeactivateCustomerCommand toCommand(CustomerDeactivateRequest request);

    CustomerDeactivateResponse toResponse(DeactivateCustomerResult result);

    default Email mapEmail(String value) {
        return value == null ? null : new Email(value);
    }

    default String map(Email email) {
        return email == null ? null : email.value();
    }

    default PhoneNumber mapPhoneNumber(String value) {
        return value == null ? null : new PhoneNumber(value);
    }

    default String map(PhoneNumber phoneNumber) {
        return phoneNumber == null ? null : phoneNumber.value();
    }

    // Convert UUID to CustomerSegmentId
    default CustomerSegmentId map(UUID value) {
        return value == null ? null : new CustomerSegmentId(value);
    }

    // Convert CustomerSegmentId to UUID
    default UUID map(CustomerSegmentId segmentId) {
        return segmentId == null ? null : segmentId.value();
    }
}
