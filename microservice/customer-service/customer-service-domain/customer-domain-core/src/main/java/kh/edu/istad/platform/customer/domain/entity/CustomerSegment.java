package kh.edu.istad.platform.customer.domain.entity;

import kh.edu.istad.common.domain.entity.AggregateRoot;
import kh.edu.istad.platform.customer.domain.exception.CustomerSegmentDomainException;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentId;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentType;

import java.util.UUID;

public class CustomerSegment extends AggregateRoot<CustomerSegmentId> {

    private CustomerSegmentType type;
    private String description;

    public CustomerSegment() {
    }

    public CustomerSegment(
            CustomerSegmentType type,
            String description
    ) {
        this.type = type;
        this.description = description;
    }

    public void initiate() {
        if (getId() != null) {
            throw new CustomerSegmentDomainException(
                    "Customer segment ID must be null before initialization"
            );
        }

        setId(new CustomerSegmentId(UUID.randomUUID()));
        validate();
    }

    public void update(
            CustomerSegmentType type,
            String description
    ) {
        this.type = type;
        this.description = description;
        validate();
    }

    private void validate() {
        if (type == null) {
            throw new CustomerSegmentDomainException(
                    "Customer segment type is required"
            );
        }

        if (description != null && description.length() > 500) {
            throw new CustomerSegmentDomainException(
                    "Description must not exceed 500 characters"
            );
        }
    }

    public CustomerSegmentType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}