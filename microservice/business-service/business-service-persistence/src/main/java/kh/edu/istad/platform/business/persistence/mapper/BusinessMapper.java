package kh.edu.istad.platform.business.persistence.mapper;

import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.valueobject.BusinessContact;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.BusinessName;
import kh.edu.istad.platform.business.persistence.entity.BusinessDocument;

public class BusinessMapper {

    private BusinessMapper() {
    }

    public static BusinessDocument toDocument(Business business) {
        return new BusinessDocument(
                business.getBusinessId().value().toString(),
                business.getBusinessName().legalName(),
                business.getBusinessName().displayName(),
                business.getBusinessContact().email(),
                business.getBusinessContact().phoneNumber(),
                business.isActive()
        );
    }

    public static Business toDomain(BusinessDocument document) {
        return Business.reconstitute(
                BusinessId.of(document.getId()),
                new BusinessName(
                        document.getLegalName(),
                        document.getDisplayName()
                ),
                new BusinessContact(
                        document.getEmail(),
                        document.getPhoneNumber()
                ),
                document.isActive()
        );
    }
}