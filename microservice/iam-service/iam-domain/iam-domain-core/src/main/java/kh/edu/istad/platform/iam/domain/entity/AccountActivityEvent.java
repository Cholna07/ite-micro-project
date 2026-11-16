package kh.edu.istad.platform.iam.domain.entity;

import java.time.Instant;
import java.util.UUID;

public record AccountActivityEvent(
        UUID id,
        UUID userUuid,
        AccountActivityEventType eventType,
        String description,
        Instant occurredAt,
        String ipAddress,
        String userAgent,
        String username
) {
    public AccountActivityEvent(UUID id, UUID userUuid, AccountActivityEventType eventType, String description,
                                Instant occurredAt, String ipAddress, String userAgent) {
        this(id, userUuid, eventType, description, occurredAt, ipAddress, userAgent, null);
    }
}
