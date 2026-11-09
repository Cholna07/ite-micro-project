package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record RoleAssignmentResult(
        UUID userUuid,
        UUID roleUuid,
        Instant assignedAt,
        Instant expiresAt
) {
}
