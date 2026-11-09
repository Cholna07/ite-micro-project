package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record PermissionAssignmentResult(
        UUID roleUuid,
        UUID permissionUuid,
        Instant assignedAt,
        Instant expiresAt
) {
}
