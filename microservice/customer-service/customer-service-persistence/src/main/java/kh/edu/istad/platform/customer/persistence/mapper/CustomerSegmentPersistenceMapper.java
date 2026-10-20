package kh.edu.istad.platform.customer.persistence.mapper;

import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentId;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentType;
import kh.edu.istad.platform.customersegment.persistence.document.CustomerSegmentDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerSegmentPersistenceMapper {

    @Mapping(target = "id", expression = "java(segment.getId().value().toString())")
    @Mapping(target = "type", expression = "java(segment.getType().name())")
    CustomerSegmentDocument toDocument(CustomerSegment segment);

    default CustomerSegment toDomain(CustomerSegmentDocument document) {
        CustomerSegment segment = new CustomerSegment(
                CustomerSegmentType.valueOf(document.getType()),
                document.getDescription()
        );

        // Restore the existing ID without generating a new one.
        segment.setId(new CustomerSegmentId(UUID.fromString(document.getId())));
        return segment;
    }
}