package kh.edu.istad.platform.iam.domain.dto.result;

import java.time.Instant;
import java.util.UUID;

public record UserRoleAssignmentView(UUID roleUuid, String roleName, Instant assignedAt, Instant expiresAt) {
}
