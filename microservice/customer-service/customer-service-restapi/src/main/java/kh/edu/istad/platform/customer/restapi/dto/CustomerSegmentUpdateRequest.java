package kh.edu.istad.platform.customer.restapi.dto;

import kh.edu.istad.platform.customer.domain.valueobject.CustomerSegmentType;

public record CustomerSegmentUpdateRequest(
        CustomerSegmentType type,
        String description
) {
}