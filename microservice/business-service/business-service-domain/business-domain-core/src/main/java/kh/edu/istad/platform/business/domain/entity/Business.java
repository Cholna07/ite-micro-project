package kh.edu.istad.platform.business.domain.entity;

import kh.edu.istad.platform.business.domain.exception.BusinessDomainException;
import kh.edu.istad.platform.business.domain.valueobject.BusinessContact;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.BusinessName;

public class Business {

    private final BusinessId businessId;
    private final BusinessName businessName;
    private final BusinessContact businessContact;
    private boolean active;

    private Business(
            BusinessId businessId,
            BusinessName businessName,
            BusinessContact businessContact,
            boolean active
    ) {
        this.businessId = businessId;
        this.businessName = businessName;
        this.businessContact = businessContact;
        this.active = active;
    }

    public static Business register(
            String legalName,
            String displayName,
            String email,
            String phoneNumber
    ) {
        try {
            BusinessName name = new BusinessName(
                    legalName,
                    displayName
            );

            BusinessContact contact = new BusinessContact(
                    email,
                    phoneNumber
            );

            return new Business(
                    BusinessId.generate(),
                    name,
                    contact,
                    true
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessDomainException(
                    exception.getMessage()
            );
        }
    }

    public static Business reconstitute(
            BusinessId businessId,
            BusinessName businessName,
            BusinessContact businessContact,
            boolean active
    ) {
        if (businessId == null
                || businessName == null
                || businessContact == null) {
            throw new BusinessDomainException(
                    "Business data cannot be null"
            );
        }

        return new Business(
                businessId,
                businessName,
                businessContact,
                active
        );
    }

    public BusinessId getBusinessId() {
        return businessId;
    }

    public BusinessName getBusinessName() {
        return businessName;
    }

    public BusinessContact getBusinessContact() {
        return businessContact;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        if (!active) {
            throw new BusinessDomainException(
                    "Business is already inactive"
            );
        }

        active = false;
    }
}