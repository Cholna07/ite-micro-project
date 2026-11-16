package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record UserPermissionAssignmentView(UUID permissionUuid, String permissionName, String effect, Instant assignedAt, Instant expiresAt) {
}
