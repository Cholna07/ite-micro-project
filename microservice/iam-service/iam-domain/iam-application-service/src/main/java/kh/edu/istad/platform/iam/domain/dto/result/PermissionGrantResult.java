package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record PermissionGrantResult(
        UUID userUuid,
        UUID permissionUuid,
        String effect,
        Instant assignedAt,
        Instant expiresAt
) {
}