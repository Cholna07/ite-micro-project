package kh.edu.istad.platform.customer.persistence.mapper;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.persistence.document.CustomerDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;

@Mapper (componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerPersistenceMapper {
    @Mapping(source = "id.id",target="id")
    @Mapping(source = "email.value",target ="email")
    @Mapping(source = "phoneNumber.value",target = "phoneNumber")
    CustomerDocument toDocument(Customer customer);

    @Mapping(source = "id",target="id.id")
    @Mapping(source = "email",target ="email.value")
    @Mapping(source = "phoneNumber",target = "phoneNumber.value")
    Customer toDomain(CustomerDocument customerDocument);
}
