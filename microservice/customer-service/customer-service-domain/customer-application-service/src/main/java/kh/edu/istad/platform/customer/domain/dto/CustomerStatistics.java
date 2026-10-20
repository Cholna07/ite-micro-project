package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record CustomerStatistics(
        String status,
        UUID segmentId,
        long totalCustomers
) {
}