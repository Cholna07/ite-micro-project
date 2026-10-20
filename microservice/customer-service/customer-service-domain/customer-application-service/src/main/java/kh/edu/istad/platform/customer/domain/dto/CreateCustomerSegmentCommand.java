package kh.edu.istad.platform.customer.domain.dto;

import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentType;

public record CreateCustomerSegmentCommand(
        CustomerSegmentType type,
        String description
) {
}