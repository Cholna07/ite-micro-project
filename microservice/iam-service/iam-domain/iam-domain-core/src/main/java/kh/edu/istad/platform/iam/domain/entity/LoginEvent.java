package kh.edu.istad.platform.iam.domain.entity;

import java.time.Instant;
import java.util.UUID;

public record LoginEvent(
        UUID id,
        Long userId,
        UUID userUuid,
        LoginEventOutcome outcome,
        Instant occurredAt,
        String ipAddress,
        String userAgent,
        String failureCategory
) {
}
