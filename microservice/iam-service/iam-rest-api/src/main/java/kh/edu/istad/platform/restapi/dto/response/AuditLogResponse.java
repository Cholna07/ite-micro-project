package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.domain.entity.AuditLogEvent;
import kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(UUID id, UUID actorUuid, String action, String targetType, String targetId,
                               AuditLogOutcome outcome, Instant occurredAt, String ipAddress,
                               String userAgent, String details) {
    public static AuditLogResponse from(AuditLogEvent event) {
        return new AuditLogResponse(event.id(), event.actorUuid(), event.action(), event.targetType(),
                event.targetId(), event.outcome(), event.occurredAt(), event.ipAddress(),
                event.userAgent(), event.details());
    }
}
