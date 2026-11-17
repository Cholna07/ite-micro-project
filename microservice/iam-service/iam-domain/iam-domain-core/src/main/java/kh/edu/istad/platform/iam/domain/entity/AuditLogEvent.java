package kh.edu.istad.platform.iam.domain.entity;

import java.time.Instant;
import java.util.UUID;

public record AuditLogEvent(
        UUID id,
        UUID actorUuid,
        String action,
        String targetType,
        String targetId,
        AuditLogOutcome outcome,
        Instant occurredAt,
        String ipAddress,
        String userAgent,
        String details
) {
}
