package kh.edu.istad.platform.customer.restapi.dto;

import java.util.UUID;

public record CustomerSegmentResponse(
        UUID segmentId,
        String type,
        String description
) {
}