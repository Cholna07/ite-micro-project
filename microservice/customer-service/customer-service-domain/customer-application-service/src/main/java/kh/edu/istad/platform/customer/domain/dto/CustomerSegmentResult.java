package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record CustomerSegmentResult(
        UUID segmentId,
        String type,
        String description
) {
}