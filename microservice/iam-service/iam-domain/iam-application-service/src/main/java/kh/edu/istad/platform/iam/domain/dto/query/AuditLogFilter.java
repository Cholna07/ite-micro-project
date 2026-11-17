package kh.edu.istad.platform.iam.domain.dto.query;

import kh.edu.istad.platform.iam.domain.entity.AuditLogOutcome;

import java.time.Instant;
import java.util.UUID;

public record AuditLogFilter(
        String keyword,
        UUID actorUuid,
        String action,
        String targetType,
        String targetId,
        AuditLogOutcome outcome,
        Instant from,
        Instant to,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
