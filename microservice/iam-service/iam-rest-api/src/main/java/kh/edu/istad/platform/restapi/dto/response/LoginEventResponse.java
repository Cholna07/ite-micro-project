package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.domain.entity.LoginEventOutcome;

import java.time.Instant;
import java.util.UUID;

public record LoginEventResponse(
        UUID id,
        UUID userUuid,
        LoginEventOutcome outcome,
        Instant occurredAt,
        String ipAddress,
        String userAgent,
        String failureCategory
) {
}
