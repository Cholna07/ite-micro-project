package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record RolePermissionAssignmentView(UUID permissionUuid, String permissionName, String resource, String action, Instant assignedAt, Instant expiresAt) {
}
