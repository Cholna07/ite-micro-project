package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;

import java.time.Instant;
import java.util.UUID;

public record AccountActivityEventResponse(
        UUID id,
        AccountActivityEventType eventType,
        String description,
        Instant occurredAt,
        String ipAddress,
        String userAgent
) {
}
